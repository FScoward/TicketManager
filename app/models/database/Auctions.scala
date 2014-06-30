package models.database

import play.api.db.DB
import scala.slick.driver.MySQLDriver.simple._
import play.api.Play.current

/**
 * Created by FScoward on 2014/06/29.
 */
case class Auction(eventId: String, account: String, exhibitNumber: Int, status: Int = 0)

object Auctions {
  val database = Database.forDataSource(DB.getDataSource())
  
  class Auctions(tag: Tag) extends Table[Auction](tag, "AUCTION") {
    def eventId = column[String]("EVENT_ID", O NotNull)
    def account = column[String]("ACCOUNT", O NotNull)
    def exhibitNumber = column[Int]("EXHIBIT_NUMBER", O NotNull)
    def status = column[Int]("STATUS", O NotNull)
    def * = (eventId, account, exhibitNumber, status) <> (Auction.tupled, Auction.unapply)
  }
  val auctions = TableQuery[Auctions]
  
  def insert(auction: Auction) = database.withSession { implicit session: Session =>
    auctions.insert(auction)
  }
  
  def findByEventId(eventId: String) = database.withSession { implicit session: Session =>
    auctions.where(_.eventId === eventId).list()
  }
  
  def countExhibitNumber(eventId: String) = database.withSession { implicit session: Session =>
    auctions.where(_.eventId === eventId).map(_.exhibitNumber).sum
  }
}
