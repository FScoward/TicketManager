package controllers

/**
 * Created by endlick1989 on 2014/04/29.
 */

import play.api._
import play.api.mvc._
import models.filter.AuthAction

object UserController extends Controller with AuthAction {
  def index(username: String) = AuthAction { uuid => implicit request =>

    models.database.Events.findEventByAccountId(1)
    Ok(views.html.mypage(Option(username)))
  }
}
