package controllers

import models.{EmployeeName, UpdateProfileForm, PasswordUpdateForm, Employee}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{Action, Controller}
import play.api.i18n.Messages.Implicits._

import scala.collection.mutable.ListBuffer

object EmployeeController extends Controller {

  def employee = Action { implicit request =>
      Ok(views.html.addEmployee(""))
  }

  val employeeForm = Form(mapping("name" -> text, "username" -> text, "password" -> text,
    "accountNo" -> number(min = 0),
    "phone" -> number(min = 0), "email" -> email, "address" -> text,
    "role" -> text)(Employee.apply)(Employee.unapply))

  def addEmployee = Action(parse.form(employeeForm, onErrors = (withError: Form[Employee]) =>
    Redirect("/employee"))) { implicit request =>
    val employee = request.body
    employee.addEmployee
    Ok(views.html.addEmployee("Employee "+employee.name+" Added"))
  }

  def editProfile = Action { implicit request =>
    val userName = request.session("userName")
    //val filledForm = updateEmployeeForm.fill(UpdateProfileForm(accountNo, phone, email, address))
    val (accountNo, phone, email, address):(Int,Int,String,String) = getProfile(userName)
    val filledForm = updateEmployeeForm.fill(UpdateProfileForm(accountNo,phone,email,address))
    Ok(views.html.editEmployee(filledForm,""))
  }

  def getProfile(userName:String):(Int,Int,String,String)={
    var accountNo: Int = 0
    var phone: Int = 0
    var email: String = ""
    var address: String = ""
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select accountNo,phone,email,address from employee where username=\""
        + userName + "\";")
      rs.next()
      accountNo = rs.getInt("accountNo")
      phone = rs.getInt("phone")
      email = rs.getString("email")
      address = rs.getString("address")
    }
    finally {
      conn.close()
    }
    (accountNo,phone,email,address)
  }

  val updateEmployeeForm = Form(mapping("accountNo" -> number(min = 0), "phone" -> number(min = 0), "email" -> text,
    "address" -> text)(UpdateProfileForm.apply)(UpdateProfileForm.unapply))

  def updateProfile = Action(parse.form(updateEmployeeForm, onErrors = (withError: Form[UpdateProfileForm])
  => Redirect("/editProfile"))) { implicit request =>
    val userProfile = request.body
    val userName = request.session("userName")
    userProfile.update(userName)
    val (accountNo, phone, email, address):(Int,Int,String,String) = getProfile(userName)
    val filledForm = updateEmployeeForm.fill(UpdateProfileForm(accountNo,phone,email,address))
    Ok(views.html.editEmployee(filledForm,"Profile Updated"))
  }

  val passwordForm = Form(mapping("currentPassword" -> text, "newPassword" -> text,
    "repeatPassword" -> text)(PasswordUpdateForm.apply)(PasswordUpdateForm.unapply))

  def changePassword = Action { implicit request =>
    Ok(views.html.changePassword(" "))
  }

  def updatePassword = Action(parse.form(passwordForm, onErrors = (withError: Form[PasswordUpdateForm]) =>
    Redirect("/changePassword"))) { implicit request =>
    val changePass = request.body
    val userName = request.session("userName")
    val status = changePass.updatePassword(userName)
    if(status)
    Ok(views.html.changePassword("Password Changed"))
    else
      Ok(views.html.changePassword("Password Could not be Changed"))
  }

  def getEmployeeList = {
    var employeeList = new ListBuffer[models.Employee]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select * from employee;")
      while (rs.next())
        employeeList += models.Employee(rs.getString("name"),rs.getString("userName"),rs.getString("password"),rs.getInt
          ("accountNo"),
          rs.getInt("phone"),rs.getString("email"),rs.getString("address"),rs.getString("role"))
    }
    finally conn.close()
    employeeList
  }

  def viewDeleteEmployee = Action { implicit request =>
      Ok(views.html.deleteEmployee(getEmployeeList.toList, ""))
  }

  val deleteEmployeeForm = Form(mapping("username" -> text)(EmployeeName.apply)(EmployeeName.unapply))

  def deleteEmployee = Action(parse.form(deleteEmployeeForm, onErrors = (withError: Form[EmployeeName]) =>
    Redirect("/deleteEmployee"))) { implicit request =>
    val userName = request.body.username
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("delete from employee where username=\"" + userName + "\";")
    }
    finally conn.close()
    Ok(views.html.deleteEmployee(getEmployeeList.toList, userName + " deleted"))
  }

}
