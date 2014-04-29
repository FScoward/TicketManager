package controllers

/**
 * Created by FScoward on 2014/04/26.
 */
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.database.{Event, Events}
import play.api.libs.Crypto

object EventController extends Controller {
  
  val createEventForm = Form(
    tuple(
      "eventName" -> text,
      "eventDate" -> sqlDate,
      "isPrivate" -> boolean
    )
  )
  
  def index = Action {
    Ok(views.html.createEvent())
  }

  def create = Action { implicit request =>
    createEventForm.bindFromRequest.fold(
      hasErrors => {
        Ok(views.html.createEvent())
      },
      success => {
        val (eventName, eventDate, isPrivate) = success
        val eventId = Crypto.sign(eventName + eventDate)
        play.Logger.debug("eventID: " + eventId + " || Length: " + eventId.size)
        Events.insert(Event(eventId, eventName, new java.sql.Date(eventDate.getTime), isPrivate))
        Redirect(routes.EventController.viewEventList)
      }
    )
  }
  
  def viewEventList = Action { implicit request =>
    val eventList = models.database.Events.read
    Ok(views.html.events(eventList))
  }
  
  def viewEvent(eventId: String) = Action {
    val event = Events.findEventById(eventId)
    if(event.size == 1){
      Ok(views.html.event(event.head))
    }else{
      BadRequest
    }
  }
  
}
