package controllers

/**
 * Created by endlick1989 on 2014/04/29.
 */

import play.api._
import play.api.mvc._
import models.filter.AuthAction
import models.database.{Accounts, Events}

object UserController extends Controller with AuthAction {
  def index(username: String) = AuthAction { uuid => implicit request =>
    /*
    * TODO
    * ユーザー名をAccountテーブルから検索
    * */
//    models.database.Events.findEventByAccountId(1)


    // get owner event list

    val eventList = Events.findEventByScreenName(username)



    Ok(views.html.mypage(Option(username), eventList))
  }
}
