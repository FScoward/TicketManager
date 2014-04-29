/*
package models.filter

import play.api.mvc.{SimpleResult, Result, RequestHeader, Filter}
import twitter4j.{Twitter, TwitterException, TwitterFactory}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import play.api.cache.Cache
import play.api.Play.current

/**
 * Created by FScoward on 2014/04/27.
 */

/*
* from
*     http://xawa99.blogspot.jp/2013/06/Scala-Play-Twitter4J.html
* */
class AuthFilter(withAuthActions : Seq[String]) extends Filter {

  override def apply(next : (RequestHeader) => Future[SimpleResult])
                    (request : RequestHeader) : Future[SimpleResult] = {
    val actionInvoked: String = request.tags.getOrElse(play.api.Routes.ROUTE_ACTION_METHOD, "")
    play.Logger.debug("AuthFilter called: " + actionInvoked)

    
    if (needsAuth(request)) {
      play.Logger.debug("need auth")

      val uuid = request.session.get("twitter")
      if(uuid.isDefined){
        next(request)
      }else{
        Future{auth(request)}
      }
//      next(request)
    } else {
      next(request)
    }
  }

  private def needsAuth(request : RequestHeader) : Boolean = {
    val actionInvoked: String = request.tags.getOrElse(
      play.api.Routes.ROUTE_ACTION_METHOD, "")
    if (withAuthActions.contains(actionInvoked)) {
      true
    } else {
      false
    }
  }

  private def auth(request : RequestHeader) : SimpleResult = {
    val twitter = TwitterFactory.getSingleton
    play.Logger.debug("" + request.copy())
    try{
      val requestToken = twitter.getOAuthRequestToken
      play.Logger.debug("request token is " + requestToken)
      val url = requestToken.getAuthorizationURL
      controllers.Default.Redirect(url)
    }catch {
      case e: TwitterException => controllers.Default.Unauthorized(e.getMessage)
      case e: IllegalStateException => controllers.Default.Redirect("/")
    }
  }
}
*/
