package controllers

import java.io.File

import com.github.tototoshi.csv.CSVWriter
import models.Payment
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{Action, Controller}

import scala.collection.mutable.ListBuffer


object PaymentController extends Controller {

  var userList = new ListBuffer[String]
  var vendorList = new ListBuffer[String]

  val paymentForm = Form(mapping("vendor" -> text, "amount" -> number, "description" -> text)
    (Payment.apply)(Payment.unapply))

  def addTransaction = Action(parse.form(paymentForm, onErrors = (withErrors: Form[Payment]) =>
    BadRequest("/pay"))) { implicit request =>
    val payment = request.body
    val user = request.session("userName")
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("INSERT INTO expenses (username,vendor,amount,status) VALUES (\"" + user + "\",\"" +
        payment.vendor + "\"," + payment.amount + ",\"U\");")
    }
    finally conn.close()
    Ok(views.html.payments(userList.toList, vendorList.toList, "Payment Submitted"))
  }

  def approveTransaction = Action { request =>
    ???
  }

  def denyTransaction = Action { request =>
    ???
  }

  def fetchData = {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val userSet = stmt.executeQuery("select username from employee;")
      while (userSet.next())
        userList += userSet.getString("username")
      val vendorSet = stmt.executeQuery("select name from vendor;")
      while (vendorSet.next())
        vendorList += vendorSet.getString("username")
    }
    finally {
      conn.close()
    }
  }

  def payment = Action { implicit request =>
    val userType = request.session("userType")
    if ((userType == "admin") || (userType == "super")) {
      fetchData
      Ok(views.html.payments(userList.toList, vendorList.toList, ""))
    }
    else Redirect("/")
  }

  def reviewPayments = Action { implicit request =>

    var payments = new ListBuffer[Payment]
    var userList = new ListBuffer[String]
    val conn = DB.getConnection()

    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"U\";")
      while (rs.next()) {
        val p1 = Payment(rs.getString("vendor"),
          rs.getInt("amount"), rs.getString("description"))
        userList += rs.getString("user")
        payments += p1
      }
    }
    finally {
      conn.close()
    }
    Ok(views.html.reviewPayment(userList.toList, payments.toList))
  }

  def getFile = Action { implicit request =>
    var payments = new ListBuffer[Payment]
    val conn = DB.getConnection()
    val file = new File("expenses.csv")
    val writer = CSVWriter.open(file)
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from expenses where status=\"A\";")
      while (rs.next()) {
        val p1 = Payment(rs.getString("vendor"),
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
