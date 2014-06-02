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
  play.Logger.debug(isOpen)

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

  def restTicketNum(eventId: String): Int = {
    val ticket = Tickets.findTicketByEventId(eventId)
    val attend = EventMembers.findByEventIdStatus(eventId)
    val t: Int = ticket.map{ t => {
      t.number
    }}.sum
    t - attend.length
  }

}
