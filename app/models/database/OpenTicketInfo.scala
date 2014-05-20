package models.database

/**
 * Created by FScoward on 2014/05/18.
 */
import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current

object OpenTicketInfos {
  val database = Database.forDataSource(DB.getDataSource())

  class OpenTicketInfos(tag: Tag) extends Table[(String)](tag, "OPEN_TICKET_INFO") {
    def eventId = column[String]("EVENT_ID", O NotNull)

    def * = (eventId)
  }

  val openTicketInfos = TableQuery[OpenTicketInfos]

  def insert(eventId: String) = database.withSession { implicit session: Session =>
    openTicketInfos.insert(eventId)
  }

  def findAll = database.withSession{ implicit session: Session =>
/*
    import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
    import Q.interpolation // String interpolation API

    implicit val getMember = GetResult(rs => Event(rs.nextString, rs.nextString, rs.nextString, rs.nextDate, rs.nextString, rs.nextBoolean))

    sql"select EVENT.EVENT_ID, EVENT.EVENT_NAME, EVENT.EVENT_LOCATION, EVENT.EVENT_DATE, EVENT.OWNER, EVENT.IS_PRIVATE from OPEN_TICKET_INFO inner join EVENT ON OPEN_TICKET_INFO.EVENT_ID = EVENT.EVENT_ID".as[Event].list()
    */

    val query = for{
      t <- openTicketInfos
      e <- Events.events if (t.eventId is e.eventId)
    } yield (e)

    query.list.distinct
  }

}
