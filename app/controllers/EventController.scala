package controllers

/**
 * Created by FScoward on 2014/04/26.
 */
import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.database.{Event, Events}

object EventController extends Controller {
  
  val createEventForm = Form(
    mapping(
      "eventName" -> text,
      "eventDate" -> sqlDate,
      "isPrivate" -> boolean,
      "eventId" -> optional(number)
    )(Event.apply)(Event.unapply)
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
        Events.insert(Event(success.eventName, new java.sql.Date(success.eventDate.getTime), success.isPrivate))
        Redirect(routes.EventController.viewEventList)
      }
    )
  }
  
  def viewEventList = Action {
    val eventList = models.database.Events.read
    Ok(views.html.events(eventList))
  }
  
  def viewEvent(eventId: Int) = Action {
    Ok("event id : " + eventId)
  }
  
}
