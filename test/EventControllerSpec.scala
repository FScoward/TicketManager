/**
 * Created by FScoward on 2014/04/26.
 */
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class EventControllerSpec extends Specification {
 /*
  "EventController" should {
    "render the create page" in new WithApplication() {
      val page = route(FakeRequest(GET, "/event/create")).get
      
      status(page) must equalTo(OK)
    }
  }
*/
  "EventController#findByScreenName(FF)" should {
    "return list" in new WithApplication() {
      models.database.Events.findEventByScreenName("FF", 0).size === 0
      models.database.Events.findEventByScreenName("FScoward", 0).size === 7
      models.database.Events.countEventByScreenName("FScoward") === 7
    }
  }
}
