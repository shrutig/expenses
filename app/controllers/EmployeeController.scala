package controllers

import javax.inject.Inject
import models._
import models.authorizations._
import play.api.db.DB
import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import play.api.Play.current

class EmployeeController @Inject()(
                                    val messagesApi: MessagesApi,
                                    val env: Environment[User, CookieAuthenticator],
                                    socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, CookieAuthenticator] {

  private val EMAIL = "email"
  private val ROLE = "role"
  private val USER_NAME = "userName"

  def employee = SecuredAction(WithRole(SuperAdmin)).async { implicit request =>
    Future.successful(Ok(views.html.addEmployee(getEmployeeList, "", request.identity)))
  }

  val employeeForm = Form(mapping(EMAIL -> email, ROLE -> text(maxLength = 5))(Employee.apply)(Employee.unapply))

  def addEmployee() = SecuredAction(WithRole(SuperAdmin)).async { implicit request =>
    employeeForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.addEmployee(getEmployeeList, "wrong data", request
        .identity))),
      data => {
        Role.setRoleForUser(data.email, data.role)
        Future.successful(Ok(views.html.addEmployee(getEmployeeList, s"Employee ${data.email} Changed", request
          .identity)))
      })
  }

  def getEmployeeList = {
    var employeeList = new ListBuffer[models.Employee]
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_STATE_4)
      val rs = stmt.executeQuery()
      while (rs.next())
        employeeList += models.Employee(rs.getString(USER_NAME), rs.getString(ROLE))
    }
    employeeList.toList
  }

}