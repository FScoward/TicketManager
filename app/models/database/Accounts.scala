package models.database

/**
 * Created by FScoward on 2014/04/26.
 */

import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current
import models.Authority

case class Account(account: String, accountId: Option[Int] = None)

object Accounts {
  val database = Database.forDataSource(DB.getDataSource())

  class Accounts(tag: Tag) extends Table[Account](tag, "EVENT") {
    def account = column[String]("ACCOUNT", O NotNull)
    def accountId = column[Option[Int]]("ACCOUNT_ID", O AutoInc)
    def * = (account, accountId) <> (Account.tupled, Account.unapply)
  }
  val accounts = TableQuery[Accounts]
}
