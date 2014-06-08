package models.database

import play.api.db.DB
//import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.MySQLDriver.simple._
import play.api.Play.current
import java.sql.Date
/**
 * Created by FScoward on 2014/04/26.
 */
case class Event(eventId: String, eventName: String, eventLocation: String, eventDate: Date, owner: String, isPrivate: Boolean)

object Events {
  val database = Database.forDataSource(DB.getDataSource())

  class Events(tag: Tag) extends Table[Event](tag, "EVENT") {
    def eventId = column[String]("EVENT_ID", O NotNull)
    def eventName = column[String]("EVENT_NAME", O NotNull)
    def eventLocation = column[String]("EVENT_LOCATION", O NotNull)
    def eventDate = column[Date]("EVENT_DATE", O NotNull)
    def owner = column[String]("OWNER", O NotNull)
    def isPrivate = column[Boolean]("IS_PRIVATE", O NotNull)
    def ownerFK = foreignKey("owner_fk", owner, Accounts.accounts)(_.account)

    def * = (eventId, eventName, eventLocation, eventDate, owner, isPrivate) <> (Event.tupled, Event.unapply)
  }
 
  val events = TableQuery[Events]
  
  def insert(event: Event) = database.withTransaction { implicit session: Session =>
    events.insert(event)
    EventAdmins.eventAdmins.insert(EventAdmin(event.eventId, event.owner))
  }
  
  def read(page: Int) = database.withSession { implicit session: Session =>
    val query = events.where(_.isPrivate === false)
    (query.drop(page * 10).take(10).list, query.length.run)
  }
  
  def findEventById(eventId: String) = database.withSession { implicit session: Session =>
    events.where(_.eventId === eventId).list
  }

  def findEventByScreenName(screenName: String, page: Int) = database.withSession { implicit session: Session =>
    val query = for{
      m <- Accounts.accounts if (m.account === screenName)
      em <- EventMembers.eventMembers if (em.account === screenName)
      e <- events if (e.eventId === em.eventId) && (em.account === screenName)
    } yield (e)

    (query.drop(page * 10).take(10).list(), query.length.run)
  }

  def deleteEvent(screenName: String, eventId: String) = database.withSession { implicit session: Session =>
    events.where(_.eventId === eventId).where(_.owner === screenName).delete
  }

}
