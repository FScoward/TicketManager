package models.filter

import play.api.mvc.{Result, Action, Request, AnyContent}

/**
 * Created by FScoward on 2014/04/29.
 */
trait AuthAction {
  def AuthAction(f: Request[AnyContent] => Result): Action[AnyContent] = {
    Action{ request =>
      
      // auth
      
      f(request)
    }
  } 
}
