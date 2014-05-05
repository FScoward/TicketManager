package models.database

/**
 * Created by endlick1989 on 2014/04/30.
 */
import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current

sealed abstract class AttendStatus
case object Attendance extends AttendStatus
case object Absense extends AttendStatus
case object Undecide extends AttendStatus

case class EventMember(eventId: String, account: String, authority: String, attendStatus: Int = 0)
object EventMembers {
  val database = Database.forDataSource(DB.getDataSource())

  class EventMembers(tag: Tag) extends Table[EventMember](tag, "EVENT_MEMBER") {
    def eventId = column[String]("EVENT_ID")
    def account = column[String]("ACCOUNT")
    def authority = column[String]("AUTHORITY")
    def attendStatus = column[Int]("ATTEND_STATUS")
    def eventFK = foreignKey("event_fk", eventId, Events.events)(_.eventId)
    def accountFK = foreignKey("account_fk", account, Accounts.accounts)(_.account)
    def * = (eventId, account, authority, attendStatus) <> (EventMember.tupled, EventMember.unapply)
  }

  val eventMembers = TableQuery[EventMembers]

  def insert(event: Event, account: String) = database.withTransaction { implicit session: Session =>
    Events.events.insert(event)
    eventMembers.insert(EventMember(event.eventId, account, "Normal"))
  }

  def findAccountByEventId(eventId: String) = database.withSession { implicit session: Session =>
    eventMembers.where(_.eventId === eventId).list().map(_.account)
  }

  def updateStatus(eventId: String, account: String, attendStatus: String) = database.withTransaction { implicit session: Session =>
    val code = attendStatus match {
      case "attend" => 1
      case "absence" => 2
      case "undecided" => 3
    }

    eventMembers
      .filter(_.eventId === eventId)
      .filter(_.account === account)
      .update(EventMember(eventId, account, code))
  }
}
