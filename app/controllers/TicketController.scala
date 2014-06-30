package controllers

/**
 * Created by FScoward on 2014/05/11.
 */

import play.api._

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.filter.AuthAction
import models.database._
import play.api.i18n.Messages
import org.h2.jdbc.JdbcSQLException
import models.database.Ticket
import scala.slick.jdbc.StaticQuery0

object TicketController extends Controller with AuthAction {
  val ticketForm = Form(
    tuple(
      "number" -> number(min = 1),
      "ticketHolder" -> nonEmptyText
    )
  )

  def createTicket(eventId: String) = AuthAction { uuid => implicit request =>
    ticketForm.bindFromRequest.fold(
      hasError => {
        val errorMessages = hasError.errors.map{x =>
          x.key + " : " + Messages(x.message)
        }
        Redirect(routes.EventController.viewEvent(eventId)).flashing("errorMessages" -> errorMessages.mkString)
      },
      success => {
        val (number, ticketHolder) = success
        val ticket = Ticket(eventId, number, ticketHolder)

        if(Accounts.findAccountByAccount(ticketHolder).size == 0){
          Redirect(routes.EventController.viewEvent(eventId)).flashing("errorMessages" -> "please input registerd user")
        }else{
          try{
            Tickets.insert(ticket)
            Redirect(routes.EventController.viewEvent(eventId))
          }catch{
            case e: JdbcSQLException => {
              Redirect(routes.EventController.viewEvent(eventId)).flashing("errorMessages" -> "既に登録されていませんか？")
            }
          }
        }
      }
    )
  }

  /**
   * チケット情報の削除
   *
   * @param ticketId チケットID
   * */
  def deleteTicket(ticketId: Int) = AuthAction{ uuid => implicit request =>
    Tickets.deleteTicketByTicketId(ticketId)

    Redirect(request.headers.get("Referer").get)
  }

  def updateTicketStatus = AuthAction { uuid => implicit request =>
    val post = request.body.asFormUrlEncoded
    val eventId = post.get("eventId").head
    val ticketId = post.get("ticketId").head.toInt
    val status = post.get("status").head.toInt

    Tickets.updateStatusByTicketId(ticketId, status)
    Redirect(request.headers.get("Referer").get)
  }

  /**
   * 余りチケットの公開/非公開
   * */
  def openRestTicketInfo = AuthAction{ uuid => implicit request =>
    val post = request.body.asFormUrlEncoded
    val eventId = post.get("eventId").head
    val isOpen = post.get("isOpen").head

    if(isOpen == "true") {
      OpenTicketInfos.insert(eventId)
    } else {
      OpenTicketInfos.delete(eventId)
    }
    Redirect(request.headers.get("Referer").get)
  }

  /**
   * 公開チケットの情報取得
   * */
  def viewOpenTicket = AuthAction{ uuid => implicit request =>
    val eventList: List[Event] = OpenTicketInfos.findAll
    val e: List[(Event, Int)] = eventList.map{ event => (event, restTicketNum(event.eventId)) }
    Ok(views.html.tickets(e.filter(_._2 > 0)))
  }

  /**
   * 余剰チケット枚数
   *
   * @param eventId イベントID
   * */
  def restTicketNum(eventId: String): Int = {
    val ticket = Tickets.findTicketByEventId(eventId)
    val attend = EventMembers.findByEventIdStatus(eventId)
    val t: Int = ticket.map{ t => {
      t.number
    }}.sum
    t - attend.length
  }

  /**
   * チケット検索
   * */
  def searchTicket = AuthAction { uuid => implicit request =>
    val word = request.getQueryString("word").get
    val list = Tickets.findTicketByWord(word)
    val result: List[(Event, Int)] = list.map{ event => (event, restTicketNum(event.eventId))}

    Ok(views.html.tickets(result.filter(_._2 > 0)))
  }
  
  /**
   * オークション担当者登録
   * */
  def auction = AuthAction { uuid => implicit request =>
    val exhibitNumber = request.getQueryString("exhibitNumber")
    val responsible = request.getQueryString("responsible")
    val eventId = request.getQueryString("eventId")
    
    // イベントに紐づけて出品チケット枚数、担当者を登録
    if (exhibitNumber.isDefined && responsible.isDefined && eventId.isDefined) {
      try{
        Auctions.insert(Auction(eventId.get, responsible.get, exhibitNumber.get.toInt))
        Redirect(request.headers.get("Referer").get)
      } catch {
        // 本当は MySQLIntegrityConstraintViolationException を拾う
        case e: Exception => {
          Redirect(request.headers.get("Referer").get).flashing("errorMessages" -> "エラーが発生しました。(ユーザー名に間違いはありませんか？)")
        }
      }
    } else {
      Redirect(request.headers.get("Referer").get).flashing("errorMessages" -> "エラーが発生しました。")
    }
  }
  
 }
