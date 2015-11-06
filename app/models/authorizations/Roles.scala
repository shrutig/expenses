package models.authorizations

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import play.api.db.DB
import play.api.i18n._
import play.api.mvc.Request
import play.api.Play.current
import scala.concurrent.Future

/**
 * Check for authorization
 */
case class WithRole(role: Role) extends Authorization[User, CookieAuthenticator] {

  override def isAuthorized[B](user: User, authenticator: CookieAuthenticator)(implicit request: Request[B],
                                                                               messages: Messages):
  Future[Boolean] = {
    user.roles match {
      case list: Role => Future.successful(list.eq(role) || list.eq(SuperAdmin))
      case _ => Future.successful(false)
    }
  }
}

/**
 * Trait for all roles
 */
trait Role {
  def name: String
}

/**
 * Companion object
 */
object Role {

  def apply(role: String): Role = role match {
    case SuperAdmin.name => SuperAdmin
    case Admin.name => Admin
    case SimpleUser.name => SimpleUser
    case _ => Unknown
  }

  def unapply(role: Role): Option[String] = Some(role.name)

  def getRoleForUser(email: String): Role = {
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_STATE_2)
      stmt.setString(1, email)
      val rs = stmt.executeQuery()
      if (rs.next()) {
        apply(rs.getString(1))
      }
      else {
        Unknown
      }
    }
  }

  def setRoleForUser(email: String, role: String) = {

    val userRole = getRoleForUser(email)
    if (userRole.equals(Unknown)) {
      DB.withConnection { conn =>
        val stmt = conn.prepareStatement(models.sqlStatement.EMP_STATE_1)
        stmt.setString(1, email)
        stmt.setString(2, role)
        stmt.executeUpdate()
      }
    }
    else {
      DB.withConnection { conn =>
        val stmt = conn.prepareStatement(models.sqlStatement.EMP_STATE_3)
        stmt.setString(1, role)
        stmt.setString(2, email)
        stmt.executeUpdate()
      }
    }
  }


}

/**
 * Super Administration role
 */
object SuperAdmin extends Role {
  val name = "super"
}

/**
 * Administration role
 */
object Admin extends Role {
  val name = "admin"
}

/**
 * Normal user role
 */
object SimpleUser extends Role {
  val name = "user"
}

/**
 * The generic unknown role
 */
object Unknown extends Role {
  val name = "-"
}