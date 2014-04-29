package controllers

/**
 * Created by FScoward on 2014/04/27.
 */

import play.api._
import play.api.mvc._
import twitter4j.{TwitterException, TwitterFactory}
import play.api.cache.Cache
import play.api.Play.current
import java.util.UUID

object LoginController extends Controller {
  
  def login = Action { implicit request =>
    val twitter = TwitterFactory.getSingleton
    try{
      val requestToken = twitter.getOAuthRequestToken
      val url = requestToken.getAuthorizationURL
      Redirect(url)
    }catch {
      case e: TwitterException => Unauthorized
      case e: IllegalStateException => Unauthorized
    }
  }
  
  def logout = Action { implicit request =>
    Cache.remove("twitter")
    Redirect(routes.Application.index()).withNewSession
  }

  def callback = Action { request =>
    val oauthVerifier = request.getQueryString("oauth_verifier").get
    val twitter = TwitterFactory.getSingleton
    val accessToken = twitter.getOAuthAccessToken(oauthVerifier)
    val uuid = UUID.randomUUID.toString
    Cache.set(uuid, accessToken)
    
    Redirect(routes.Application.index()).withSession("twitter" -> uuid, "screenName" -> twitter.getScreenName)
  }
}
