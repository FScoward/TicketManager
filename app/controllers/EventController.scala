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
      hasErrors => {
        Ok(views.html.createEvent())
      },
      success => {
        val (eventName, eventLocation, eventDate, isPrivate) = success
        val eventId = Crypto.sign(eventName + eventLocation + eventDate)
        play.Logger.debug("eventID: " + eventId + " || Length: " + eventId.size)
//        Events.insert(Event(eventId, eventName, new java.sql.Date(eventDate.getTime), isPrivate))
        val event = Event(eventId, eventName, eventLocation, new java.sql.Date(eventDate.getTime), isPrivate)
        EventMembers.insert(event, request.session.get("screenName").get)

//        Redirect(routes.EventController.viewEventList)
        Redirect(routes.UserController.index(uuid))

      }
    )
  }

  /**
   * event list page
   * */
  def viewEventList = Action { implicit request =>
    val eventList = models.database.Events.read
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
