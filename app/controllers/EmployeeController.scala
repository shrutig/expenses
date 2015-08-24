package controllers

import anorm._
import models.{EmployeeName, UpdateEmployee, PasswordUpdate, Employee}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{Action, Controller}

import scala.collection.mutable.ListBuffer

object EmployeeController extends Controller {

  def employee = Action { implicit request =>
    Ok(views.html.addEmployee(""))
  }

  val employeeForm = Form(mapping("name" -> text, "username" -> text, "password" -> text, "accountNo" -> number,
    "phone" -> number, "email" -> email, "address" -> text,
    "role" -> text)(Employee.apply)(Employee.unapply))

  def addEmployee = Action(parse.form(employeeForm, onErrors = (withError: Form[Employee]) =>
    BadRequest("/employee"))) { implicit request =>
    val employee = request.body
    insertEmployee(employee)
    Ok(views.html.addEmployee("Employee"))
  }

  def insertEmployee(employee: Employee) = {
    /*
    DB.withConnection { implicit connection =>
      SQL("insert into employee(name,username,password,accountNo,phone,email,address,role) values " +
        "({name},{username},{password},{accountNo},{phone},{email},{address},{role})").on(
          'name -> employee.name, 'username -> employee.userName, 'password -> employee.password,
          'accountNo -> employee.accountNo, 'phone -> employee.phone,
          'email -> employee.email, 'address -> employee.address)
    }
    */
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("insert into employee(name,username,password,accountNo,phone,email,address,role) values (\"" +
        employee.name + "\",\"" + employee.userName + "\",\"" + employee.password + "\"," + employee.accountNo + ","
        + employee.phone + ",\"" + employee.email + "\",\"" + employee.address + "\",\"" + employee.role + "\");")
    }
    finally {
      conn.close()
    }

  }

  def editProfile = Action { implicit request =>
    Ok(views.html.editEmployee())
  }

  val updateEmployeeForm = Form(mapping("accountNo" -> number, "phone" -> number, "email" -> text,
    "address" -> text)(UpdateEmployee.apply)(UpdateEmployee.unapply))

  def updateProfile = Action(parse.form(updateEmployeeForm, onErrors = (withError: Form[UpdateEmployee])
  => BadRequest("/editDetails"))) { implicit request =>
    val details = request.body
    val userName = request.session("userName")
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update employee set accountNo=" + details.accountNo + ",phone=" + details.phone + ",email=\"" +
        details.email + "\",address=\"" + details.address + "\" where username=\"" + userName + "\";")
    }
    finally {
      conn.close()
    }
    Redirect("/userHome")
  }

  val passwordForm = Form(mapping("currentPassword" -> text, "newPassword" -> text,
    "repeatPassword" -> text)(PasswordUpdate.apply)(PasswordUpdate.unapply))

  def changePassword = Action { implicit request =>
    Ok(views.html.changePassword(" "))
  }

  def updatePassword = Action(parse.form(passwordForm, onErrors = (withError: Form[PasswordUpdate]) =>
    Redirect("/changePassword"))) { implicit request =>
    val changePass = request.body
    val userName = request.session("userName")

    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("select password from employee where username=\"" + userName + "\";")
      if (rs.next()) {
        val currentPass = rs.getString("password")
        if (changePass.newPassword == changePass.repeatPassword)
          stmt.execute("update employee set password=\"" + changePass.newPassword +
            "\" where username=\"" + userName + "\";")
              }
    }
    finally conn.close()
    Ok(views.html.changePassword("Password Changed"))
  }

  def viewDeleteEmployee = Action { implicit request =>
    var employeeList = new ListBuffer[String]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select username from employee;")
      while (rs.next())
        employeeList += rs.getString("username")
    }
    finally conn.close()
    Ok(views.html.deleteEmployee(employeeList.toList))
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
    Redirect("/deleteEmployee")
  }


}
