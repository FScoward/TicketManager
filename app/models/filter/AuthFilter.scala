package models.filter

import play.api.mvc.{SimpleResult, Result, RequestHeader, Filter}
import twitter4j.{TwitterException, TwitterFactory}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

/**
 * Created by FScoward on 2014/04/27.
 */

/*
* from
*     http://xawa99.blogspot.jp/2013/06/Scala-Play-Twitter4J.html
* */
class AuthFilter(withoutAuthActions : Seq[String]) extends Filter {

  override def apply(next : (RequestHeader) => Future[SimpleResult])
                    (request : RequestHeader) : Future[SimpleResult] = {
    val actionInvoked: String = request.tags.getOrElse(play.api.Routes.ROUTE_ACTION_METHOD, "")
    play.Logger.debug("AuthFilter called: " + actionInvoked)

    if (needsAuth(request)) {
      Future{auth(request)}
    } else {
      play.Logger.debug("AuthFilter called: " + actionInvoked)
      next(request)
    }
  }

  private def needsAuth(request : RequestHeader) : Boolean = {
    val actionInvoked: String = request.tags.getOrElse(
      play.api.Routes.ROUTE_ACTION_METHOD, "")
    if (!withoutAuthActions.contains(actionInvoked)) {
//      TwitterHolder.getAccessToken == null

      true
    } else {
      false
    }
  }

  private def auth(request : RequestHeader) : SimpleResult = {
    val twitter = TwitterFactory.getSingleton
    try{
      val requestToken = twitter.getOAuthRequestToken
      val url = requestToken.getAuthorizationURL
      controllers.Default.Redirect(url)
    }catch {
      case e: TwitterException => controllers.Default.Unauthorized(e.getMessage)
    }
  }
}
