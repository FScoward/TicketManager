package controllers

/**
 * Created by FScoward on 2014/04/27.
 */

import play.api._
import play.api.mvc._
import twitter4j.{Twitter, TwitterException, TwitterFactory}
import play.api.cache.Cache
import play.api.Play.current
import java.util.UUID
import twitter4j.auth.{AccessToken, RequestToken}

object LoginController extends Controller {
  
  def login = Action { implicit request =>
    try{
      val twitter = TwitterFactory.getSingleton
      twitter.setOAuthAccessToken(null)
      val requestToken = twitter.getOAuthRequestToken
      val url = requestToken.getAuthorizationURL
      Cache.set(requestToken.getToken, requestToken)
      Redirect(url)
    }catch {
      case e: TwitterException => Unauthorized(e.getMessage)
      case e: IllegalStateException => Unauthorized(e.getMessage)
    }
  }
  
  def logout = Action { implicit request =>
    Cache.remove("twitter")
    Redirect(routes.Application.index()).withNewSession
  }

  def callback = Action { request =>
    request.getQueryString("oauth_verifier") match {
      case Some(oauthVerifier) => {
        val oauthToken = request.getQueryString("oauth_token").get
        val requestToken = Cache.getAs[RequestToken](oauthToken).get
        val twitter = TwitterFactory.getSingleton
        val accessToken: AccessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier)
        val uuid = UUID.randomUUID.toString
        Cache.set(uuid, accessToken)
        val screenName = accessToken.getScreenName
        twitter.setOAuthAccessToken(null)

        if(models.database.Accounts.findAccountByAccount(screenName).size == 0){
          models.database.Accounts.insert(models.database.Account(screenName))
        }

        Redirect(routes.UserController.index(screenName)).withSession("twitter" -> uuid, "screenName" -> screenName)
      }
      case None => Redirect("/")
    }
  }
}
