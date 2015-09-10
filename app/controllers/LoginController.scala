package controllers

import java.sql.SQLException

import models.{Navigation, User}
import play.api.Play.current
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.mvc._

import scala.collection.mutable.ListBuffer

object LoginController extends Controller {

  def index = Action {
    Ok(views.html.index("")).withNewSession
  }

  val userForm = Form(mapping("username" -> text, "password" -> text)(User.apply)(User.unapply))
  var role: String = ""

  def authenticate = Action(parse.form(userForm, onErrors = (withError: Form[User]) =>
    BadRequest(views.html.index("Enter Fields")))) { implicit request =>
    val userData = request.body
    val user = userData.name
    print(user)
    val password = userData.password
    if (authenticateUser(user, password)) {
      print("logged in ")
      println(role)
      val session = Session(Map("userType" -> role, "userName" -> user))
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
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select username,password,role from employee where username=\"" + loginName + "\";")
      rs.next
      val user = User(rs.getString("username"), rs.getString("password"))
      role = rs.getString("role")
      if (user.checkPassword(password)) true
      else false
    }
    catch {
      case e: SQLException => false
    }
    finally {
      conn.close()
    }
  }

}
