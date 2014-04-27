package models.database

import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current
import java.sql.Date

/**
 * Created by FScoward on 2014/04/26.
 */
case class Event(eventName: String, eventDate: Date, isPrivate: Boolean, eventId: Option[Int] = None)

object Events {
  val database = Database.forDataSource(DB.getDataSource())

  class Events(tag: Tag) extends Table[Event](tag, "EVENT") {
    def eventName = column[String]("EVENT_NAME", O NotNull)
    def eventDate = column[Date]("EVENT_DATE", O NotNull)
    def isPrivate = column[Boolean]("IS_PRIVATE", O NotNull)
    def eventId = column[Option[Int]]("EVENT_ID", O AutoInc)
    def * = (eventName, eventDate, isPrivate, eventId) <> (Event.tupled, Event.unapply)
  }
 
  val events = TableQuery[Events]
  
  def insert(event: Event) = database.withSession { implicit session: Session =>
    events.insert(event)
  }
  
  def read = database.withSession { implicit session: Session =>
    events.list
  }
  
  def findEventById(eventId: Int) = database.withSession { implicit session: Session =>
    events.where(_.eventId === eventId).list
  }

}
