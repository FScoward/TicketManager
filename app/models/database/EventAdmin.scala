package models.database

import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current
import java.sql.Date

/**
 * Created by FScoward on 2014/05/16.
 */
case class EventAdmin(eventId: String, adminAccount: String)

object EventAdmins {
  val database = Database.forDataSource(DB.getDataSource())

  class EventAdmins(tag: Tag) extends Table[EventAdmin](tag, "EVENT_ADMIN") {
    def eventId = column[String]("EVENT_ID", O NotNull)
    def adminAccount = column[String]("ADMIN_ACCOUNT", O NotNull)
    def adminAccountFK = foreignKey("admin_account_fk", adminAccount, Accounts.accounts)(_.account)

    def * = (eventId, adminAccount) <> (EventAdmin.tupled, EventAdmin.unapply)
  }

  val eventAdmins = TableQuery[EventAdmins]

  def insert(eventAuth: EventAdmin) = database.withSession { implicit session: Session =>
    eventAdmins.insert(eventAuth)
  }

  def findByEventId(eventId: String) = database.withSession { implicit session: Session =>
    eventAdmins.where(_.eventId === eventId).map(_.adminAccount).list
  }

}
