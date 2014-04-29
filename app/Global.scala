import models.filter.AuthFilter
import play.api.GlobalSettings
import play.api.mvc.WithFilters

/**
 * Created by FScoward on 2014/04/27.
 */
object Global extends WithFilters(
  AuthFilter("index", "logout", "callback")) with GlobalSettings

object AuthFilter {
  def apply(withoutAuthActions : String*) =
    new AuthFilter(withoutAuthActions)
}

