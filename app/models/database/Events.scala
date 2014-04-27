package models.database

import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current
import java.sql.Date

/**
 * Created by FScoward on 2014/04/26.
 */
case class Event(eventId: String, eventName: String, eventDate: Date, isPrivate: Boolean)

object Events {
  val database = Database.forDataSource(DB.getDataSource())

  class Events(tag: Tag) extends Table[Event](tag, "EVENT") {
    def eventId = column[String]("EVENT_ID", O NotNull)
    def eventName = column[String]("EVENT_NAME", O NotNull)
    def eventDate = column[Date]("EVENT_DATE", O NotNull)
    def isPrivate = column[Boolean]("IS_PRIVATE", O NotNull)
    def * = (eventId, eventName, eventDate, isPrivate) <> (Event.tupled, Event.unapply)
  }
 
  val events = TableQuery[Events]
  
  def insert(event: Event) = database.withSession { implicit session: Session =>
    events.insert(event)
  }
  
  def read = database.withSession { implicit session: Session =>
    events.list
  }
  
  def findEventById(eventId: String) = database.withSession { implicit session: Session =>
    events.where(_.eventId === eventId).list
  }
}
