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
        val eventId = Crypto.sign(eventName + eventLocation + eventDate)
        val event = Event(eventId, eventName, eventLocation, new java.sql.Date(eventDate.getTime), isPrivate)
        EventMembers.insert(event, request.session.get("screenName").get)

        Redirect(routes.EventController.viewEvent(eventId))
      }
    )
  }

  /**
   * event list page
   * */
  def viewEventList(page: Int) = Action { implicit request =>
    val eventList = models.database.Events.read(page - 1)
    Ok(views.html.events(eventList))
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

      Ok(views.html.event(event.head, groupedAccountList, ticketInfo))
    }else{
      BadRequest
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

}
