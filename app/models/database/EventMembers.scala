package models.database

/**
 * Created by endlick1989 on 2014/04/30.
 */
import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current

case class EventMember(eventId: String, account: String, authority: String)
object EventMembers {
  val database = Database.forDataSource(DB.getDataSource())

  class EventMembers(tag: Tag) extends Table[EventMember](tag, "EVENT_MEMBER") {
    def eventId = column[String]("EVENT_ID")
    def account = column[String]("ACCOUNT")
    def authority = column[String]("AUTHORITY")
    def eventFK = foreignKey("event_fk", eventId, Events.events)(_.eventId)
    def accountFK = foreignKey("account_fk", account, Accounts.accounts)(_.account)
    def * = (eventId, account, authority) <> (EventMember.tupled, EventMember.unapply)
  }

  val eventMembers = TableQuery[EventMembers]

  def insert(event: Event, account: String) = database.withTransaction { implicit session: Session =>
    Events.events.insert(event)
    eventMembers.insert(EventMember(event.eventId, account, "Normal"))
  }
}
