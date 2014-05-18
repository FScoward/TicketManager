package models.database

/**
 * Created by FScoward on 2014/05/18.
 */
import java.sql.Date

import play.api.db.DB
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current
import scala.slick.ast.ColumnOption.NotNull
import scala.slick.model.ForeignKeyAction
import org.joda.time.LocalDateTime
import com.github.tototoshi.slick.H2JodaSupport._

case class Comment(eventId: String, commentId: Int = 0, comment: String, account: String, createDate: LocalDateTime = new LocalDateTime)

object Comments {
  val database = Database.forDataSource(DB.getDataSource())

  class Comments(tag: Tag) extends Table[Comment](tag, "COMMENT") {
    def eventId = column[String]("EVENT_ID", O NotNull)
    def commentId = column[Int]("COMMENT_ID", O NotNull)
    def comment = column[String]("COMMENT", O NotNull)
    def account = column[String]("ACCOUNT", O NotNull)
    def createDate = column[LocalDateTime]("CREATE_DATE", O NotNull)

    def eventFK = foreignKey("event_fk", eventId, Events.events)(_.eventId, onDelete = ForeignKeyAction.Cascade)
    def accountFK = foreignKey("account_fk", account, Accounts.accounts)(_.account)
    def * = (eventId, commentId, comment, account, createDate) <> (Comment.tupled, Comment.unapply)
  }

  val comments = TableQuery[Comments]

  def insert(comment: Comment) = database.withSession { implicit session: Session =>
    val commentId = comments.where(_.eventId === comment.eventId).map(_.commentId).list
    if(commentId.size == 0){
      val com = comment.copy(commentId = 1)
      comments.insert(com)
    } else {
      val com = comment.copy(commentId = commentId.size + 1)
      comments.insert(com)
    }
  }

  def findByEventId(eventId: String) = database.withSession { implicit session: Session =>
    comments.where(_.eventId === eventId).sortBy(_.commentId).list
  }

}
