package controllers

import models.User
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc._

import scala.collection.mutable.ListBuffer

object LoginController extends Controller {


  def index = Action {
    Ok(views.html.index("Login"))
  }

  val userForm = Form(mapping("username" -> text, "password" -> text)(User.apply)(User.unapply))
  var role: String = ""

  def authenticate = Action(parse.form(userForm, onErrors = (withError: Form[User]) =>
    BadRequest(views.html.index("Enter Fields")))) { implicit request =>
    val userData = request.body
    val user = userData.name
    val password = userData.password
    if (authenticateUser(user, password)) {
      Ok(views.html.home("Welcome")).withSession("userType" -> role, "userName" -> user)
    } else {
      Ok(views.html.index("Enter correct details"))
    }
  }

  def home = Action { implicit request =>
    Ok(views.html.home(""))
  }

  def logout = Action {
    Ok(views.html.index("Logged out")).withSession("userType" -> "clear")
  }

  def authenticateUser(loginName: String, password: String): Boolean = {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select username,password,role from employee where username=\"" + loginName + "\";")
      rs.next
      val user = User(rs.getString("username"), rs.getString("password"))
      role = rs.getString("role")
      if (user.name == loginName) {
        if (user.checkPassword(password)) true
        else false
      }
      else false
    }
    finally {
      conn.close()
    }
  }


}

/* def changePassword(userName: String, password: String) = Action {
   DB.withConnection { implicit conn =>
     SQL("UPDATE users SET password={newPassword} WHERE name={userName};"
     ).on("newPassword" -> password, "userName" -> userName).executeUpdate()
   }
   Ok(views.html.user(userName))
 }
*/

