package controllers

import java.io.File
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.{Date, Calendar}

import com.github.tototoshi.csv.CSVWriter
import models.{PaymentReview, PaymentForm}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{ Action, Controller}

import scala.collection.mutable.ListBuffer


object PaymentController extends Controller {

  val paymentForm = Form(mapping("vendor" -> text, "amount" -> number, "description" -> text)
    (PaymentForm.apply)(PaymentForm.unapply))

  def addTransaction = Action(parse.form(paymentForm, onErrors = (withErrors: Form[PaymentForm]) =>
    Redirect("/pay"))) { implicit request =>
    val payment = request.body
    val user = request.session("userName")
    val conn = DB.getConnection()
    var stmt:Statement = null
    try {
      stmt = conn.createStatement()
      stmt.execute("INSERT INTO expenses (username,vendor,amount,status,description) VALUES (\"" + user + "\",\"" +
        payment.vendor + "\"," + payment.amount + ",\"U\",\"" + payment.description + "\");")
    }
    finally {
      stmt.close()
      conn.close()
    }
    Ok(views.html.payments(VendorController.getVendorNameList toList, "Transaction with " + payment.vendor + " of Rs " +
      payment.amount + " added"))
  }

  def payment = Action { implicit request =>
    Ok(views.html.payments(VendorController.getVendorNameList toList, ""))
  }


  def approveTransaction(id: Int, choice: Int) = Action { implicit request =>
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update expenses set status=\"A\" where id=" + id + ";")
    }
    finally conn.close()
    if (choice == 1)
      Redirect("/reviewPay")
    else
      Redirect("/deniedTransactions")
  }

  def denyTransaction(id: Int) = Action { implicit request =>
    val adminName = request.session("userName")
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update expenses set status=\"D\" where id=" + id + ";")
      stmt.execute("update expenses set admin=\"" + adminName + "\" where id=" + id + ";")
    }
    finally conn.close()
    Redirect("/reviewPay")
  }

  def processTransaction(id: Int) = Action(parse.multipartFormData) { implicit request =>
    var fileName: String = null
    request.body.file("receipt").map { receipt =>
      receipt.ref.moveTo(new File("public/receipts/" + receipt.filename))
      fileName = receipt.filename
    }.getOrElse {
      Ok(views.html.acceptedTransactions(getAcceptedPayments.toList, "File could not be uploaded"))
    }
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update expenses set status=\"P\" , fileName=\"" + fileName + "\" where id=" + id + ";")
    }
    finally conn.close()
    Ok(views.html.acceptedTransactions(getAcceptedPayments.toList, "File has been uploaded"))
  }


  def reviewPayments = Action { implicit request =>
    var payments = new ListBuffer[PaymentReview]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"U\";")
      while (rs.next()) {
        val p1 = PaymentReview(rs.getInt("id"), rs.getString("username"), rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"), rs.getString("admin"), rs.getString("fileName"))
        payments += p1
      }
    }
    finally {
      conn.close()
    }
    Ok(views.html.reviewPayment(payments.toList))
  }

  def viewDeniedTransactions = Action { implicit request =>
    var payments = new ListBuffer[PaymentReview]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"D\";")
      while (rs.next()) {
        val p1 = PaymentReview(rs.getInt("id"), rs.getString("username"), rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"), rs.getString("admin"), rs.getString("fileName"))
        payments += p1
      }
    }
    finally {
      conn.close()
    }
    Ok(views.html.deniedTransactions(payments.toList))
  }

  def viewAcceptedTransactions = Action { implicit request =>
    Ok(views.html.acceptedTransactions(getAcceptedPayments.toList, ""))
  }

  def getAcceptedPayments = {
    var payments = new ListBuffer[PaymentReview]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"A\";")
      while (rs.next()) {
        val p1 = PaymentReview(rs.getInt("id"), rs.getString("username"), rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"), rs.getString("admin"), rs.getString("fileName"))
        payments += p1
      }
    }
    finally {
      conn.close()
    }
    payments
  }

  def viewProcessedTransactions = Action { implicit request =>
    var payments = new ListBuffer[PaymentReview]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"P\";")
      while (rs.next()) {
        val p1 = PaymentReview(rs.getInt("id"), rs.getString("username"), rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"), rs.getString("admin"), rs.getString("fileName"))
        payments += p1
      }
    }
    finally {
      conn.close()
    }
    Ok(views.html.processedTransactions(payments.toList))
  }


  def getFile = Action { implicit request =>
    var payments = new ListBuffer[PaymentForm]
    val date:Date = Calendar.getInstance().getTime()
    val simpleDate: SimpleDateFormat= new SimpleDateFormat("yyyyMMdd.hhmmss")
    val str:String = "expenses"+simpleDate.format(date) + ".csv"
    val file = new File(str)
    val writer = CSVWriter.open(file)
    writer.writeRow(List("username", "vendor", "amount", "description"))
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"A\";")
      while (rs.next()) {
        val p1 = PaymentForm(rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"))
        payments += p1
        writer.writeRow(List(rs.getString("username"), p1.vendor, p1.amount, p1.description))
      }
    }
    finally {
      conn.close()
      writer.close()
    }
    Ok.sendFile(new File(str))
  }

  def getReceipt(fileName: String) = Action { implicit request =>
    Ok.sendFile(new File("public/receipts/" + fileName))
  }

}
