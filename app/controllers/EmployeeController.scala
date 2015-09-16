package controllers

import jp.t2v.lab.play2.auth.AuthElement
import models.Role.Admin
import models._
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import play.api.i18n.Messages.Implicits._

object EmployeeController extends Controller with AuthElement with AuthConfigImpl {

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


  def employee = Action {
      implicit request => Ok(views.html.addEmployee(""))
  }

  val employeeForm = Form(mapping(NAME -> text(maxLength = 15), USER_NAME -> text(maxLength = 10), PASSWORD -> text
    (maxLength = 8), ACCOUNT_NO -> number(min = 0),
    PHONE -> number(min = 0), EMAIL -> email, ADDRESS -> text(maxLength = 20),
    ROLE -> text(maxLength = 5))(Employee.apply)(Employee.unapply))

  def addEmployee = Action(parse.form(employeeForm, onErrors = (withError: Form[Employee]) =>
    Redirect("/employee"))) { implicit request =>
    val employee = request.body
    Account.create(Account(employee.userName, employee.password, employee.name, employee.accountNo,
      employee.phone, employee.email, employee.address, Role.valueOf(employee.role)))
    Ok(views.html.addEmployee(s"Employee ${employee.name} Added"))
  }

  def editProfile = Action { implicit request =>
    val userName = request.session(USER_NAME)
    val filledForm = getProfile(userName)
    Ok(views.html.editEmployee(filledForm, ""))
  }

  def getProfile(userName: String): Form[UpdateProfileForm] = {
    Account.findById(userName) match {
      case Some(account) => updateEmployeeForm.fill(UpdateProfileForm(account.accountNo, account.phone, account.email,
        account.address))
    }
  }

  val updateEmployeeForm = Form(mapping(ACCOUNT_NO -> number(min = 0), PHONE -> number(min = 0), EMAIL -> text,
    ADDRESS -> text(maxLength = 20))(UpdateProfileForm.apply)(UpdateProfileForm.unapply))

  def updateProfile = Action(parse.form(updateEmployeeForm, onErrors = (withError: Form[UpdateProfileForm])
  => Redirect("/editProfile"))) { implicit request =>
    val userProfile = request.body
    val userName = request.session(USER_NAME)
    Account.updateAccount(userName, userProfile)
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
    val status = Account.updatePassword(userName, changePass)
    if (status)
      Ok(views.html.changePassword("Password Changed"))
    else
      Ok(views.html.changePassword("Password Could not be Changed"))
  }

  def getEmployeeList = {
    Account.findAll()
  }

  def viewDeleteEmployee = Action { implicit request =>
    Ok(views.html.deleteEmployee(getEmployeeList.toList, ""))
  }

  val deleteEmployeeForm = Form(mapping(USER_NAME -> text)(EmployeeName.apply)(EmployeeName.unapply))

  def deleteEmployee = Action(parse.form(deleteEmployeeForm, onErrors = (withError: Form[EmployeeName]) =>
    Redirect("/deleteEmployee"))) { implicit request =>
    val userName = request.body.userName
    Account.deleteAccount(userName)
    Ok(views.html.deleteEmployee(getEmployeeList.toList, s"$userName deleted"))
  }

}
