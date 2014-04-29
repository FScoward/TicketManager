package controllers

/**
 * Created by FScoward on 2014/04/27.
 */

import play.api._
import play.api.mvc._
import twitter4j.{TwitterFactory}
import play.api.cache.Cache
import play.api.Play.current
import java.util.UUID

object LoginController extends Controller {
  def login = Action {
    val twitter = TwitterFactory.getSingleton
    Ok
  }

  def callback = Action { request =>
    val oauthVerifier = request.getQueryString("oauth_verifier").get

    val twitter = TwitterFactory.getSingleton
    val accessToken = twitter.getOAuthAccessToken(oauthVerifier)

    play.Logger.debug(twitter.getScreenName)

    val uuid = UUID.randomUUID.toString

    Cache.set(uuid, twitter)

    Redirect(routes.Application.index()).withSession("twitter" -> uuid)
  }
}
