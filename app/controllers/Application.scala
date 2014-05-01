package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action { implicit request =>
    val screenName: Option[String] = request.session.get("screenName")
    Ok(views.html.index(screenName))
  }

}