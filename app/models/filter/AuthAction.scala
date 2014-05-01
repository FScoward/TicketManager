package models.filter

import play.api.mvc._
import play.api.cache.Cache
import play.mvc.Results.Redirect
import play.api.Play.current
import controllers.routes
import twitter4j.auth.AccessToken

/**
 * Created by FScoward on 2014/04/29.
 */
trait AuthAction {

  def username(request: RequestHeader): Option[String] = {
    request.session.get("twitter").map(x => {
      Cache.get(x).map(_.asInstanceOf[AccessToken]).map(_.getScreenName)
    }).getOrElse(None)
  }
  def onUnauthorized(request: RequestHeader) = {
    // TODO -- エラーメッセージ出力
    Results.Redirect(routes.Application.index()).withNewSession
  }

  def AuthAction(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { u =>
      Action(request => f(u)(request))
    }
  }
}
