package models.database

import play.api.db.DB
//import scala.slick.driver.H2Driver.simple._
import scala.slick.driver.MySQLDriver.simple._
import play.api.Play.current
import scala.slick.model.ForeignKeyAction
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

/**
 * Created by FScoward on 2014/05/10.
 */
case class Ticket(eventId: String, number: Int, ticketHolder: String, status: Int = 0, ticketId: Option[Int] = None)
object Tickets {
  val database = Database.forDataSource(DB.getDataSource())

  class Tickets(tag: Tag) extends Table[Ticket](tag, "TICKET") {
    def ticketId = column[Option[Int]]("TICKET_ID", O NotNull, O AutoInc)
    def eventId = column[String]("EVENT_ID", O NotNull)
    def number = column[Int]("NUMBER", O NotNull)
    def ticketHolder = column[String]("TICKET_HOLDER", O NotNull)
    def status = column[Int]("STATUS", O NotNull)
    def eventFK = foreignKey("event_fk", eventId, Events.events)(_.eventId, onDelete = ForeignKeyAction.Cascade)
    def accountFK = foreignKey("account_fk", ticketHolder, Accounts.accounts)(_.account)
    def * = (eventId, number, ticketHolder, status, ticketId) <> (Ticket.tupled, Ticket.unapply)
  }

  private val tickets = TableQuery[Tickets]
  implicit val getEventResult = GetResult(r => Event(r.<<, r.<<, r.<<, r.<<, r.<<, r.<<))

  def insert(ticket: Ticket) = database.withSession { implicit session: Session =>
    tickets.insert(ticket)
  }

  def findTicketByEventId(eventId: String) = database.withSession { implicit session: Session =>
    tickets.where(_.eventId === eventId).list
  }

  def deleteTicketByTicketId(ticketId: Int) = database.withSession { implicit session: Session =>
    tickets.where(_.ticketId === ticketId).delete
  }

  def updateStatusByTicketId(ticketId: Int, status: Int) = database.withSession { implicit session: Session =>
    tickets.where(_.ticketId === ticketId).map(_.status).update(status)
  }
  def findTicketByWord(word: String) = database.withSession { implicit session: Session =>
    sql"""
    select
      e.EVENT_ID,
      e.EVENT_NAME,
      e.EVENT_LOCATION,
      e.EVENT_DATE,
      e.OWNER,
      e.IS_PRIVATE
    from
      EVENT e,
      OPEN_TICKET_INFO oti
    where
      oti.EVENT_ID = e.EVENT_ID
      AND
      MATCH(EVENT_NAME, EVENT_LOCATION, OWNER)
      AGAINST($word)
    order by EVENT_DATE
      """
      .as[Event].list()
  }
}
