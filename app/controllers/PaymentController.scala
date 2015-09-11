package controllers

import java.io.File
import java.sql.{PreparedStatement, Statement}
import java.text.SimpleDateFormat
import java.util.{Date, Calendar}

import com.github.tototoshi.csv.CSVWriter
import models.{PaymentReview, PaymentForm}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{Action, Controller}

import scala.collection.mutable.ListBuffer


object PaymentController extends Controller {
  private val ID = "id"
  private val USER_NAME = "userName"
  private val VENDOR = "vendor"
  private val VENDOR_ACCOUNT_NO = "accountNo"
  private val VENDOR_BANK_DETAIL = "bankDetail"
  private val AMOUNT = "amount"
  private val DESCRIPTION = "description"
  private val ADMIN = "admin"
  private val FILE_NAME = "fileName"
  private val STATUS_ACCEPTED = "A"
  private val STATUS_DENIED = "D"
  private val STATUS_PROCESSED = "P"
  private val STATUS_INCOMPLETE = "I"

  val paymentForm = Form(mapping(VENDOR -> text(maxLength = 20), AMOUNT -> number, DESCRIPTION -> text(maxLength = 20))
    (PaymentForm.apply)(PaymentForm.unapply))

  def addTransaction = Action(parse.form(paymentForm, onErrors = (withErrors: Form[PaymentForm]) =>
    Redirect("/pay"))) { implicit request =>
    val payment = request.body
    val user = request.session(USER_NAME)

    DB.withConnection() { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(models.sqlStatement.PAY_STATE_1)
      stmt.setString(1, user)
      stmt.setString(2, payment.vendor)
      stmt.setInt(3, payment.amount)
      stmt.setString(4, STATUS_INCOMPLETE)
      stmt.setString(5, payment.description)
      stmt.executeUpdate()
    }
    Ok(views.html.payments(VendorController.getVendorNameList toList, s"Transaction with  ${payment.vendor} of " +
      s"Rs ${payment.amount} added"))
  }

  def payment = Action { implicit request =>
    Ok(views.html.payments(VendorController.getVendorNameList toList, ""))
  }


  def approveTransaction(id: Int, choice: Int) = Action { implicit request =>
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_2)
      stmt.setString(1, STATUS_ACCEPTED)
      stmt.setInt(2, id)
      stmt.execute()
    }
    if (choice == 1)
      Redirect("/reviewPay")
    else
      Redirect("/deniedTransactions")
  }

  def denyTransaction(id: Int) = Action { implicit request =>
    val adminName = request.session(USER_NAME)
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_3)
      stmt.setString(1, STATUS_DENIED)
      stmt.setString(2, adminName)
      stmt.setInt(3, id)
      stmt.execute()
    }
    Redirect("/reviewPay")
  }

  def processTransaction(id: Int) = Action(parse.multipartFormData) { implicit request =>
    var fileName: String = null
    request.body.file("receipt").map { receipt =>
      receipt.ref.moveTo(new File("public/receipts/" + receipt.filename))
      fileName = receipt.filename
    }.getOrElse {
      Ok(views.html.acceptedTransactions(getPayments(STATUS_ACCEPTED), "File could not be uploaded"))
    }
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_4)
      stmt.setString(1, STATUS_PROCESSED)
      stmt.setString(2, fileName)
      stmt.setInt(3, id)
      stmt.execute()
    }
    Ok(views.html.acceptedTransactions(getPayments(STATUS_ACCEPTED), "File has been uploaded"))
  }

  def getPayments(status: String): List[PaymentReview] = {
    var payments = new ListBuffer[PaymentReview]
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_5)
      stmt.setString(1, status)
      val rs = stmt.executeQuery()
      while (rs.next()) {
        val p1 = PaymentReview(rs.getInt(ID), rs.getString(USER_NAME), rs.getString
          (VENDOR),
          rs.getInt(AMOUNT), rs.getString(DESCRIPTION), rs.getString(ADMIN), rs.getString(FILE_NAME))
        payments += p1
      }
    }
    payments toList
  }

  def reviewPayments = Action { implicit request =>
    Ok(views.html.reviewPayment(getPayments(STATUS_INCOMPLETE)))
  }

  def viewDeniedTransactions = Action { implicit request =>
    Ok(views.html.deniedTransactions(getPayments(STATUS_DENIED)))
  }

  def viewAcceptedTransactions = Action { implicit request =>
    Ok(views.html.acceptedTransactions(getPayments(STATUS_ACCEPTED), ""))
  }

  def viewProcessedTransactions = Action { implicit request =>
    Ok(views.html.processedTransactions(getPayments(STATUS_PROCESSED)))
  }


  def getFile = Action { implicit request =>
    val date: Date = Calendar.getInstance().getTime()
    val simpleDate: SimpleDateFormat = new SimpleDateFormat("yyyyMMdd.hhmmss")
    val str: String = "expenses" + simpleDate.format(date) + ".csv"
    val file = new File(str)
    val writer = CSVWriter.open(file)
    writer.writeRow(List(USER_NAME, VENDOR, VENDOR_ACCOUNT_NO, VENDOR_BANK_DETAIL, AMOUNT, DESCRIPTION))
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_5)
      stmt.setString(1, STATUS_ACCEPTED)
      val rs = stmt.executeQuery()
      while (rs.next()) {
        val p1 = PaymentForm(rs.getString(VENDOR),
          rs.getInt(AMOUNT), rs.getString(DESCRIPTION))
        val stmt1 = conn.prepareStatement(models.sqlStatement.PAY_STATE_6)
        stmt1.setString(1, p1.vendor)
        val rs1 = stmt1.executeQuery()
        if (rs1.next()) {
          writer.writeRow(List(rs.getString(USER_NAME), p1.vendor, rs1.getString(VENDOR_ACCOUNT_NO), rs1.getString
            (VENDOR_BANK_DETAIL), p1.amount, p1.description))
        }
      }
    }
    Ok.sendFile(new File(str))
  }

  def getReceipt(fileName: String) = Action { implicit request =>
    Ok.sendFile(new File("public/receipts/" + fileName))
  }

}
