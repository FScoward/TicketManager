package controllers

/**
 * Created by endlick1989 on 2014/04/29.
 */

import play.api._
import play.api.mvc._
import models.filter.AuthAction
import models.database.{Accounts, Events}

object UserController extends Controller with AuthAction {
  def index(username: String, page: Int) = AuthAction { uuid => implicit request =>
    /*
    * TODO
    * ユーザー名をAccountテーブルから検索
    * */
//    models.database.Events.findEventByAccountId(1)

    // get owner event list
    val (eventList, count) = Events.findEventByScreenName(username, page - 1)

    // pagination をいくつ表示するか
    val paginationCount = {
      (count / 10) + {
        if((count % 10) > 0) 1
        else 0
      }
    }

    Ok(views.html.mypage(Option(username), eventList, paginationCount, page))
  }
}
