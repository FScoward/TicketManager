package controllers

/**
 * Created by FScoward on 2014/04/26.
 */
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.database._
import play.api.libs.Crypto
import models.filter.AuthAction
import play.api.mvc.Security.Authenticated
import models.database.Event
import models.database.EventMember
import play.api.i18n.Messages
import org.h2.jdbc.JdbcSQLException

object EventController extends Controller with AuthAction{
  
  val createEventForm = Form(
    tuple(
      "eventName" -> text,
      "eventLocation" -> text,
      "eventDate" -> sqlDate,
      "isPrivate" -> boolean
    )
  )
  
  def index = AuthAction { uuid => implicit request =>
    Ok(views.html.createEvent())
  }

  /**
   * create event
   * */
  def create = AuthAction { uuid => implicit request =>
    createEventForm.bindFromRequest.fold(
      hasError => {
        val errorMessages = hasError.errors.map{x =>
          x.key + " : " + Messages(x.message)
        }

        Redirect(request.headers.get("Referer").get).flashing("errorMessages" -> errorMessages.mkString)
      },
      success => {
        val (eventName, eventLocation, eventDate, isPrivate) = success
        val screenName = request.session.get("screenName").get
        val eventId = Crypto.sign(eventName + eventLocation + eventDate + screenName)
        val event = Event(eventId, eventName, eventLocation, new java.sql.Date(eventDate.getTime), screenName, isPrivate)

        try{
          EventMembers.insert(event, screenName)
          Redirect(routes.EventController.viewEvent(eventId))
        }catch{
          case e: JdbcSQLException => {
            Redirect(request.headers.get("Referer").get).flashing("errorMessages" -> "既に登録されていませんか？")
          }
        }
      }
    )
  }

  /**
   * event list page
   * */
  def viewEventList(page: Int) = Action { implicit request =>
    val (eventList, count) = models.database.Events.read(page - 1)

    val paginationCount = {
      (count / 10) + {
        if((count % 10) > 0) 1
        else 0
      }
    }

    Ok(views.html.events(eventList, paginationCount, page))
  }

  /**
   * view event
   * */
  def viewEvent(eventId: String) = Action { implicit request =>

    val event = Events.findEventById(eventId)
    if(event.size == 1){
      val accountList: List[EventMember] = EventMembers.findAccountByEventId(eventId)
      val groupedAccountList: Map[Int, List[EventMember]] = accountList.groupBy(_.attendStatus)

      // get ticket info
      val ticketInfo = Tickets.findTicketByEventId(eventId)

      // get admin user
      val adminAccounts = EventAdmins.findByEventId(eventId)

      // get comment
      val commentList = Comments.findByEventId(eventId)

      val isOpenTicketInfo = if(OpenTicketInfos.countByEventId(eventId) == 1) true else false

      Ok(views.html.event(event.head, groupedAccountList, ticketInfo, adminAccounts, commentList, isOpenTicketInfo))
    }else{
      BadRequest("存在しないページです。")
    }
  }

  /**
   * attend button pushed
   * */
  def attend(eventId: String) = AuthAction { uuid => implicit request =>

    val status = request.body.asFormUrlEncoded.get("status").head

    // EventMembers の更新 key = event id + account name
    EventMembers.updateStatus(eventId, uuid, status)

    Redirect(routes.EventController.viewEvent(eventId))
  }

  def deleteEvent = AuthAction { uuid => implicit request =>
    val eventId = request.body.asFormUrlEncoded.get("eventId").head
    Events.deleteEvent(uuid, eventId)

    Redirect(routes.UserController.index(uuid))
  }

  def addAdministrator = AuthAction { uuid => implicit request =>
    val post = request.body.asFormUrlEncoded.get
    val account = post.get("account").get.head
    val eventId = post.get("eventId").get.head

    EventAdmins.insert(EventAdmin(eventId, account))

    Redirect(routes.EventController.viewEvent(eventId))
  }

  def comment = AuthAction { uuid => implicit request =>
    val post = request.body.asFormUrlEncoded
    val eventId = post.get("eventId").head
    val comment = post.get("comment").head

    Comments.insert(Comment(eventId = eventId, comment = comment, account = uuid))
    Redirect(routes.EventController.viewEvent(eventId))
  }

}
