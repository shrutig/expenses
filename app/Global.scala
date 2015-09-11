//package views

import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.Application
import play.api.GlobalSettings
import play.api.db.DB
import play.api.mvc.{Handler, RequestHeader}
import play.api.mvc.Results._
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val START_STATE_1: String = "select * from employee where role = 'super';"
    val passHash = BCrypt.hashpw("super1", BCrypt.gensalt())
    val START_STATE_2: String = "INSERT INTO employee VALUES('super1',?,'super',1234,1234,'super@tuplejump" +
      ".com','asd','super');"
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(START_STATE_1)
      val rs = stmt.executeQuery()
      if (!rs.next()) {
        val stmt1 = conn.prepareStatement(START_STATE_2)
        stmt1.setString(1, passHash)
        stmt1.executeUpdate()
      }
    }
    super.onStart(app)
  }

  private val ADMIN = "admin"
  private val USER = "user"
  private val SUPER = "super"
  private val USER_TYPE = "userType"

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val path = request.path
    val users: ListBuffer[String] = ListBuffer(SUPER, ADMIN)
    (request.method) match {
      case "GET" => path match {
        case "/" => super.onRouteRequest(request)
        case "/employee" | "/deleteEmployee" | "/reviewPay" | "/deniedTransactions" => check(users - ADMIN, request)
        case "/vendor" | "/deleteVendor" | "/getFile" | "/acceptedTransactions" | "/processedTransactions" |
             "/getReceipt" => check(users, request)
        case "/pay" | "/logout" | "/changePassword" | "/editProfile" => check(users ++ ListBuffer(USER), request)
        case _ => super.onRouteRequest(request)
      }
      case "POST" => path match {
        case "/employee" | "/deleteEmployee" | "/approve" | "/deny" | "/process" => check(users - ADMIN, request)
        case "/vendor" | "/deleteVendor" => check(users, request)
        case "/pay" | "/updatePassword" | "/updateProfile" => check(users ++ ListBuffer(USER), request)
        case _ => super.onRouteRequest(request)
      }
      case _ => super.onRouteRequest(request)
    }
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    try {
      val userType = request.session(USER_TYPE)
      Future.successful(NotFound(views.html.home("")(request.session)))
    }
    catch {
      case exception: NoSuchElementException => Future.successful(NotFound(views.html.index("")))
    }

  }


  def check(users: ListBuffer[String], request: RequestHeader) = {
    var status = false
    try {
      val userType = request.session(USER_TYPE)
      for (user <- users) {
        if (userType == user) {
          status = true
        }
      }
      if (status) super.onRouteRequest(request)
      else Some(controllers.LoginController.home)
    }
    catch {
      case exception: NoSuchElementException => Some(controllers.LoginController.index)
    }
  }

}
