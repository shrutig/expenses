//package views

import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.Application
import play.api.GlobalSettings
import play.api.db.DB
import play.api.mvc.{Handler, RequestHeader}

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
        println("dhkd")
        val stmt1 = conn.prepareStatement(START_STATE_2)
        stmt1.setString(1,passHash)
        stmt1.executeUpdate()
      }
    }
    super.onStart(app)
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    val path = request.path
    (request.method) match {
      case "GET" => if (path == "/")
        super.onRouteRequest(request)
      else if (path == "/employee" || path == "/deleteEmployee" || path == "/reviewPay" ||
        path == "/deniedTransactions") {
        checkSuper(request)
      }
      else if (path == "/vendor" || path == "/deleteVendor" || path == "/getFile" || path ==
        "/acceptedTransactions" || path == "/processedTransactions" || path.startsWith("/getReceipt"))
        checkSuperOrAdmin(request)
      else
        super.onRouteRequest(request)

      case "POST" => if (path == "/employee" || path == "/deleteEmployee" || path.startsWith("/approve") ||
        path.startsWith("/deny") || path.startsWith("/process")) {
        checkSuper(request)
      }
      else if (path == "/vendor" || path == "/deleteVendor") {
        checkSuperOrAdmin(request)
      }
      else
        super.onRouteRequest(request)
    }
  }

  def checkSuper(request: RequestHeader) = {
    val userType = request.session("userType")
    if (userType == "super") super.onRouteRequest(request)
    else Some(controllers.LoginController.home)
  }

  def checkSuperOrAdmin(request: RequestHeader) = {
    val userType = request.session("userType")
    if (userType == "super" || userType == "admin") super.onRouteRequest(request)
    else Some(controllers.LoginController.home)
  }


}
