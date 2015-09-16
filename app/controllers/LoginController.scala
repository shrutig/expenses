package controllers

import models.{Account,User,Role}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import jp.t2v.lab.play2.auth._
import scala.concurrent.{ Future}
import scala.concurrent.ExecutionContext.Implicits.global

object LoginController extends Controller with LoginLogout with AuthConfigImpl {

  private val USER_NAME = "userName"
  private val PASSWORD = "password"
  private val USER_TYPE = "userType"
  private val ROLE = "role"

  def login = Action { implicit request =>
    Ok(views.html.index(""))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded.map(_.flashing(
      "success" -> "You've been logged out"
    ))
  }

  val userForm = Form(mapping(USER_NAME -> text(maxLength = 10), PASSWORD -> text(maxLength = 8))(User.apply)(User
    .unapply))
  var role: String = ""

  def authenticate = Action(parse.form(userForm)) { implicit request =>
    val userData = request.body
    val user = userData.userName
    val password = userData.password
    if (Account.authenticate(user, password) != None) {
       Account.findById(user) match {
         case Some(account) =>{
      val session = Session (Map (USER_TYPE -> Role.toString (account.role), USER_NAME -> user) )
           gotoLoginSucceeded(user)
      Ok (views.html.home ("") (session) ).withSession (session)
      }
       }

    } else {
      Ok(views.html.index("Enter proper Fields"))
    }
  }

  def home = Action { implicit request =>
    Ok(views.html.home(""))
  }

}
