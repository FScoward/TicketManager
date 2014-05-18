package controllers

/**
 * Created by FScoward on 2014/05/11.
 */

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import models.filter.AuthAction
import models.database.{Accounts, Tickets, Ticket}
import play.api.i18n.Messages
import org.h2.jdbc.JdbcSQLException

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
    // TODO
    val post = request.body.asFormUrlEncoded
    val eventId = post.get("eventId").head
    val ticketId = post.get("ticketId").head.toInt
    val status = post.get("status").head.toInt

    Tickets.updateStatusByTicketId(ticketId, status)

    play.Logger.debug("eventId: " + eventId + " status: " + status)

    Redirect(request.headers.get("Referer").get)
  }

}
