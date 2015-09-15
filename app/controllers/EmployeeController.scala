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

  private val NAME = "name"
  private val USER_NAME = "userName"
  private val PASSWORD = "password"
  private val ACCOUNT_NO = "accountNo"
  private val PHONE = "phone"
  private val EMAIL = "email"
  private val ADDRESS = "address"
  private val ROLE = "role"
  private val CURRENT_PASS = "currentPassword"
  private val NEW_PASS = "newPassword"
  private val REPEAT_PASS = "repeatPassword"


  def employee = Action { implicit request =>
    Ok(views.html.addEmployee(""))
  }

  val employeeForm = Form(mapping(NAME -> text(maxLength = 15), USER_NAME -> text(maxLength = 10), PASSWORD -> text
    (maxLength = 8), ACCOUNT_NO -> number(min = 0),
    PHONE -> number(min = 0), EMAIL -> email, ADDRESS -> text(maxLength = 20),
    ROLE -> text(maxLength = 5))(Employee.apply)(Employee.unapply))

  def addEmployee = Action(parse.form(employeeForm, onErrors = (withError: Form[Employee]) =>
    Redirect("/employee"))) { implicit request =>
    val employee = request.body
    employee.addEmployee
    Ok(views.html.addEmployee(s"Employee ${employee.name} Added"))
  }

  def editProfile = Action { implicit request =>
    val userName = request.session(USER_NAME)
    val filledForm = getProfile(userName)
    Ok(views.html.editEmployee(filledForm, ""))
  }

  def getProfile(userName: String): Form[UpdateProfileForm] = {
    var accountNo: Int = 0
    var phone: Int = 0
    var email: String = ""
    var address: String = ""
    var filledForm:Form[UpdateProfileForm] =null
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_CONST_STATE_1)
      stmt.setString(1, userName)
      val rs = stmt.executeQuery()
      rs.next()
      accountNo = rs.getInt(ACCOUNT_NO)
      phone = rs.getInt(PHONE)
      email = rs.getString(EMAIL)
      address = rs.getString(ADDRESS)
      filledForm = updateEmployeeForm.fill(UpdateProfileForm(accountNo, phone, email, address))
    }
    filledForm
  }

  val updateEmployeeForm = Form(mapping(ACCOUNT_NO -> number(min = 0), PHONE -> number(min = 0), EMAIL -> text,
    ADDRESS -> text(maxLength = 20))(UpdateProfileForm.apply)(UpdateProfileForm.unapply))

  def updateProfile = Action(parse.form(updateEmployeeForm, onErrors = (withError: Form[UpdateProfileForm])
  =>Redirect("/editProfile"))) { implicit request =>
    val userProfile = request.body
    val userName = request.session(USER_NAME)
    userProfile.update(userName)
    val filledForm = getProfile(userName)
    Ok(views.html.editEmployee(filledForm, "Profile Updated"))
  }

  val passwordForm = Form(mapping(CURRENT_PASS -> text(maxLength = 8), NEW_PASS -> text(maxLength = 8),
    REPEAT_PASS -> text(maxLength = 8))(PasswordUpdateForm.apply)(PasswordUpdateForm.unapply))

  def changePassword = Action { implicit request =>
    Ok(views.html.changePassword(" "))
  }

  def updatePassword = Action(parse.form(passwordForm, onErrors = (withError: Form[PasswordUpdateForm]) =>
    Redirect("/changePassword"))) { implicit request =>
    val changePass = request.body
    val userName = request.session(USER_NAME)
    val status = changePass.updatePassword(userName)
    if (status)
      Ok(views.html.changePassword("Password Changed"))
    else
      Ok(views.html.changePassword("Password Could not be Changed"))
  }

  def getEmployeeList = {
    var employeeList = new ListBuffer[models.Employee]
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_CONST_STATE_2)
      val rs = stmt.executeQuery()
      while (rs.next())
        employeeList += models.Employee(rs.getString(NAME), rs.getString(USER_NAME), rs.getString(PASSWORD), rs.getInt
          (ACCOUNT_NO),
          rs.getInt(PHONE), rs.getString(EMAIL), rs.getString(ADDRESS), rs.getString(ROLE))
    }
    employeeList
  }

  def viewDeleteEmployee = Action { implicit request =>
    Ok(views.html.deleteEmployee(getEmployeeList.toList, ""))
  }

  val deleteEmployeeForm = Form(mapping(USER_NAME -> text)(EmployeeName.apply)(EmployeeName.unapply))

  def deleteEmployee = Action(parse.form(deleteEmployeeForm, onErrors = (withError: Form[EmployeeName]) =>
    Redirect("/deleteEmployee"))) { implicit request =>
    val userName = request.body.userName
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_CONST_STATE_3)
      stmt.setString(1, userName)
      stmt.execute()
    }
    Ok(views.html.deleteEmployee(getEmployeeList.toList, s"$userName deleted"))
  }

}
