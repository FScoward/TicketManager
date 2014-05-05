package controllers

/**
 * Created by FScoward on 2014/04/26.
 */
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.database.{EventMembers, Event, Events}
import play.api.libs.Crypto
import models.filter.AuthAction
import play.api.mvc.Security.Authenticated

object EventController extends Controller with AuthAction{
  
  val createEventForm = Form(
    tuple(
      "eventName" -> text,
      "eventDate" -> sqlDate,
      "isPrivate" -> boolean
    )
  )
  
  def index = AuthAction { uuid => implicit request =>
    Ok(views.html.createEvent())
  }

  def create = AuthAction { uuid => implicit request =>
    createEventForm.bindFromRequest.fold(
      hasErrors => {
        Ok(views.html.createEvent())
      },
      success => {
        val (eventName, eventDate, isPrivate) = success
        val eventId = Crypto.sign(eventName + eventDate)
        play.Logger.debug("eventID: " + eventId + " || Length: " + eventId.size)
//        Events.insert(Event(eventId, eventName, new java.sql.Date(eventDate.getTime), isPrivate))
        val event = Event(eventId, eventName, new java.sql.Date(eventDate.getTime), isPrivate)
        EventMembers.insert(event, request.session.get("screenName").get)


        Redirect(routes.EventController.viewEventList)
      }
    )
  }
  
  def viewEventList = Action { implicit request =>
    val eventList = models.database.Events.read
    Ok(views.html.events(eventList))
  }
  
  def viewEvent(eventId: String) = Action { implicit request =>
    val event = Events.findEventById(eventId)
    val accountList: List[String] = EventMembers.findAccountByEventId(eventId)

    if(event.size == 1){
      Ok(views.html.event(event.head, accountList))
    }else{
      BadRequest
    }
  }

  def attend(eventId: String) = AuthAction { uuid => implicit request =>

    val status = request.body.asFormUrlEncoded.get("status").head

    play.Logger.debug(status)






    // EventMembers の更新 key = event id + account name
//    EventMembers.


    Redirect(routes.EventController.viewEvent(eventId))
  }

}
