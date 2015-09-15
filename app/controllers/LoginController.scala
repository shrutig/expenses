package controllers

import models.{ User}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc._

object LoginController extends Controller {

  private val USER_NAME = "userName"
  private val PASSWORD = "password"
  private val USER_TYPE = "userType"
  private val ROLE = "role"

  def index = Action {
    Ok(views.html.index("")).withNewSession
  }

  val userForm = Form(mapping(USER_NAME -> text(maxLength = 10), PASSWORD -> text(maxLength = 8))(User.apply)(User
    .unapply))
  var role: String = ""

  def authenticate = Action(parse.form(userForm, onErrors = (withError: Form[User]) =>
    BadRequest(views.html.index("Enter Fields")))) { implicit request =>
    val userData = request.body
    val user = userData.name
    val password = userData.password
    if (authenticateUser(user, password)) {
      val session = Session(Map(USER_TYPE -> role, USER_NAME -> user))
      Ok(views.html.home("")(session)).withSession(session)
    } else {
      Ok(views.html.index("Enter proper Fields"))
    }
  }

  def home = Action { implicit request =>
    Ok(views.html.home("")).withSession(request.session)
  }

  def logout = Action {
    Redirect("/").withNewSession
  }

  def authenticateUser(loginName: String, password: String): Boolean = {
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.LOGIN_STATE)
      stmt.setString(1, loginName)
      val rs = stmt.executeQuery()
      if (rs.next) {
        val user = User(rs.getString(USER_NAME), rs.getString(PASSWORD))
        role = rs.getString(ROLE)
        if (user.checkPassword(password)) true
        else false
      }
      else false
    }
  }

}
