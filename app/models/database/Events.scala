package models.database

import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current
import java.sql.Date

/**
 * Created by FScoward on 2014/04/26.
 */
case class Event(eventId: String, eventName: String, eventLocation: String, eventDate: Date, isPrivate: Boolean)

object Events {
  val database = Database.forDataSource(DB.getDataSource())

  class Events(tag: Tag) extends Table[Event](tag, "EVENT") {
    def eventId = column[String]("EVENT_ID", O NotNull)
    def eventName = column[String]("EVENT_NAME", O NotNull)
    def eventLocation = column[String]("EVENT_LOCATION", O NotNull)
    def eventDate = column[Date]("EVENT_DATE", O NotNull)
    def isPrivate = column[Boolean]("IS_PRIVATE", O NotNull)
    def * = (eventId, eventName, eventLocation, eventDate, isPrivate) <> (Event.tupled, Event.unapply)
  }
 
  val events = TableQuery[Events]
  
  def insert(event: Event) = database.withSession { implicit session: Session =>
    events.insert(event)
  }
  
  def read = database.withSession { implicit session: Session =>
    events.where(_.isPrivate === false).list
  }
  
  def findEventById(eventId: String) = database.withSession { implicit session: Session =>
    events.where(_.eventId === eventId).list
  }

  def findEventByScreenName(screenName: String) = database.withSession { implicit session: Session =>
    val query = for{
      m <- Accounts.accounts if (m.account === screenName)
      em <- EventMembers.eventMembers if (em.account === screenName)
      e <- events if (e.eventId === em.eventId) && (em.account === screenName)
    } yield (e)

    query.list()
  }
}
