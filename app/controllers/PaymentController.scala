package controllers

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import models.{PaymentReview, PaymentForm}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{Action, Controller}

import scala.collection.mutable.ListBuffer


object PaymentController extends Controller {

  val paymentForm = Form(mapping("vendor" -> text, "amount" -> number, "description" -> text)
    (PaymentForm.apply)(PaymentForm.unapply))

  def addTransaction = Action(parse.form(paymentForm, onErrors = (withErrors: Form[PaymentForm]) =>
    BadRequest("/pay"))) { implicit request =>
    val payment = request.body
    val user = request.session("userName")
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("INSERT INTO expenses (username,vendor,amount,status,description) VALUES (\"" + user + "\",\"" +
        payment.vendor + "\"," + payment.amount + ",\"U\",\"" + payment.description + "\");")
    }
    finally conn.close()
    Redirect("/pay")
  }

  def approveTransaction(id: Int) = Action { implicit request =>
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update expenses set status=\"A\" where id=" + id + ";")
    }
    finally conn.close()
    Redirect("/reviewPay")
  }

  def denyTransaction(id: Int) = Action { implicit request =>
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update expenses set status=\"D\" where id=" + id + ";")
    }
    finally conn.close()
    Redirect("/reviewPay")
  }

  def payment = Action { implicit request =>
    var vendorList = new ListBuffer[String]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val vendorSet = stmt.executeQuery("select name from vendor;")
      while (vendorSet.next())
        vendorList += vendorSet.getString("name")
    }
    finally {
      conn.close()
    }
    Ok(views.html.payments(vendorList.toList))
  }

  def reviewPayments = Action { implicit request =>
    var payments = new ListBuffer[PaymentReview]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"U\";")
      while (rs.next()) {
        val p1 = PaymentReview(rs.getInt("id"), rs.getString("username"), rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"))
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
          rs.getInt("amount"), rs.getString("description"))
        payments += p1
      }
    }
    finally {
      conn.close()
    }
    Ok(views.html.deniedTransactions(payments.toList))
  }

  def getFile = Action { implicit request =>
    var payments = new ListBuffer[PaymentForm]
    val conn = DB.getConnection()
    val file = new File("expenses.csv")
    val writer = CSVWriter.open(file)
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
    Ok.sendFile(new File("expenses.csv"))
  }
}
