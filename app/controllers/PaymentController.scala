package controllers

import java.io.File
import java.sql.PreparedStatement
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.github.tototoshi.csv.CSVWriter
import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.authorizations.{Admin, SuperAdmin, WithRole}
import models.{User, PaymentReview, PaymentForm}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi

import scala.collection.mutable.ListBuffer
import play.api.db.DB
import play.api.Play.current
import javax.inject.Inject

import scala.concurrent.Future

class PaymentController @Inject()(
                                   val messagesApi: MessagesApi,
                                   val env: Environment[User, CookieAuthenticator],
                                   socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, CookieAuthenticator] {

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
  private val vendor = new VendorController(messagesApi, env, socialProviderRegistry)
  val paymentForm = Form(mapping(VENDOR -> text(maxLength = 20), AMOUNT -> number, DESCRIPTION -> text(maxLength = 20))
  (PaymentForm.apply)(PaymentForm.unapply))

  def addTransaction() = SecuredAction.async { implicit request =>
    paymentForm.bindFromRequest.fold(
      form => Future.successful(Ok(views.html.payments(vendor.getVendorNameList.toList, "wrong data", request
        .identity))),
      data => {
        val user = request.identity.fullName.getOrElse("None")

        DB.withConnection() { conn =>
          val stmt: PreparedStatement = conn.prepareStatement(models.sqlStatement.PAY_STATE_1)
          stmt.setString(1, user)
          stmt.setString(2, data.vendor)
          stmt.setInt(3, data.amount)
          stmt.setString(4, STATUS_INCOMPLETE)
          stmt.setString(5, data.description)
          stmt.executeUpdate()
        }

        Future.successful(Ok(views.html.payments(vendor.getVendorNameList.toList, s"Transaction with  ${data.vendor} " +
          s"of Rs ${data.amount} added", request.identity)))
      }
    )
  }

  def payment = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.payments(vendor.getVendorNameList.toList, "", request.identity)))
  }


  def approveTransaction(id: Int, choice: Int) = SecuredAction(WithRole(SuperAdmin)).async { implicit
                                                                                             request =>
    val adminName = request.identity.fullName.getOrElse("None")
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_3)
      stmt.setString(1, STATUS_ACCEPTED)
      stmt.setString(2, adminName)
      stmt.setInt(3, id)
      stmt.execute()
    }
    if (choice == 1)
      Future.successful(Redirect("/reviewPay"))
    else
      Future.successful(Redirect("/deniedTransactions"))
  }

  def denyTransaction(id: Int) = SecuredAction(WithRole(SuperAdmin)).async { implicit request =>
    val adminName = request.identity.fullName.getOrElse("None")
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_3)
      stmt.setString(1, STATUS_DENIED)
      stmt.setString(2, adminName)
      stmt.setInt(3, id)
      stmt.execute()
    }
    Future.successful(Redirect("/reviewPay"))
  }

  def processTransaction(id: Int) = SecuredAction(WithRole(Admin)).async(parse.multipartFormData) { implicit request =>
    var fileName: String = null
    request.body.file("receipt").map { receipt =>
      receipt.ref.moveTo(new File("public/receipts/" + receipt.filename))
      fileName = receipt.filename
    }.getOrElse {
      Future.successful(Ok(views.html.acceptedTransactions(getPayments(STATUS_ACCEPTED), "File could not be " +
        "uploaded", request.identity)))
    }
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.PAY_STATE_4)
      stmt.setString(1, STATUS_PROCESSED)
      stmt.setString(2, fileName)
      stmt.setInt(3, id)
      stmt.execute()
    }
    Future.successful(Ok(views.html.acceptedTransactions(getPayments(STATUS_ACCEPTED), "File has been uploaded", request.identity)))
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
    payments.toList
  }

  def reviewPayments = SecuredAction(WithRole(SuperAdmin)).async { implicit request =>
    Future.successful(Ok(views.html.reviewPayment(getPayments(STATUS_INCOMPLETE), request.identity)))
  }

  def viewDeniedTransactions = SecuredAction(WithRole(SuperAdmin)).async { implicit request =>
    Future.successful(Ok(views.html.deniedTransactions(getPayments(STATUS_DENIED), request.identity)))
  }

  def viewAcceptedTransactions = SecuredAction(WithRole(Admin)).async { implicit request =>
    Future.successful(Ok(views.html.acceptedTransactions(getPayments(STATUS_ACCEPTED), "", request.identity)))
  }

  def viewProcessedTransactions = SecuredAction(WithRole(Admin)).async { implicit request =>
    Future.successful(Ok(views.html.processedTransactions(getPayments(STATUS_PROCESSED), request.identity)))
  }


  def getFile = SecuredAction(WithRole(Admin)).async { implicit request =>
    val date: Date = Calendar.getInstance().getTime
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
        print(rs.getString(VENDOR),
          rs.getInt(AMOUNT), rs.getString(DESCRIPTION))
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
    Future.successful(Ok.sendFile(new File(str)))
  }

  def getReceipt(fileName: String) = SecuredAction(WithRole(Admin)).async { implicit request =>
    Future.successful(Ok.sendFile(new File("public/receipts/" + fileName)))
  }

}
