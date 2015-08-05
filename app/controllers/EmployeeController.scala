package controllers

import anorm._
import models.{UpdateEmployee, PasswordUpdate, Employee}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc.{Action, Controller}

object EmployeeController extends Controller {

  def employee = Action { implicit request =>
    Ok(views.html.employee(""))
  }

  val employeeForm = Form(mapping("name" -> text, "username" -> text, "password" -> text, "accountNo" -> number,
    "phone" -> optional(number), "email" -> email, "address" -> optional(text),
    "role" -> text)(Employee.apply)(Employee.unapply))

  def addEmployee = Action(parse.form(employeeForm, onErrors = (withError: Form[Employee]) =>
    BadRequest("/employee"))) { implicit request =>
    val employee = request.body
    insertEmployee(employee)
    Ok(views.html.employee("Employee"))
  }

  def insertEmployee(employee: Employee) = {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("insert into employee (username,password,name,phone,email,address,role) values (\"" +
        employee.userName + "\",\"" + employee.password + "\",\"" + employee.name + "\"," +
        employee.phone + ",\"" + employee.email + "\",\"" + employee.address + "\",\"" + employee.role + "\");")
    }
    finally {
      conn.close()
    }
  }

  val updateEmployeeForm = Form(mapping("accountNo" -> number, "phone" -> optional(number), "email" -> text,
    "address" -> optional(text))(UpdateEmployee.apply)(UpdateEmployee.unapply))

  def updateDetails = Action { implicit request =>
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update employee set phone= ,email= ,address= ;")
    }
    finally {
      conn.close()
    }
    ???
  }

  val passwordForm = Form(mapping("currentPassword" -> text, "newPassword" -> text,
    "changePassword" -> text)(PasswordUpdate.apply)(PasswordUpdate.unapply))

  def changePassword = Action(parse.form(passwordForm, onErrors = (withError: Form[PasswordUpdate]) =>
    BadRequest("/changePassword"))) { implicit request =>
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
        else BadRequest("/changePassword")
      }
    }

    ???
  }

  /*val ds=DB.getDataSource()
  DB.withConnection { implicit connection =>
    SQL("INSERT INTO EMPLOYEE (name,dob,phone,email,address) VALUES " +
      "({name},{dob},{phone},{email},{address});").on("name" -> employee.name, "dob" -> employee.dob,
        "phone" -> employee.phone, "email" -> employee.email, "address" -> employee.address)
  }*/


}
