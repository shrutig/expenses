package controllers

import models.Role.{Admin, Super, User}
import models.{Account}
import play.api.mvc._
import jp.t2v.lab.play2.auth._
import play.api.mvc.Results._
import models.Role
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}

trait AuthConfigImpl extends AuthConfig {

  type Id = String

  type User = Account

  type Authority = Role

  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds: Int = 360000000

  override def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = {print(id)
    Future.successful(Account
    .findById(id))}

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.LoginController.home()))
  }

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.LoginController.login()))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.LoginController.login()))
  }

  def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])
                         (implicit context: ExecutionContext): Future[Result] = {
    Future.successful(Redirect(routes.LoginController.login()))
  }

  override lazy val idContainer: AsyncIdContainer[Id] = AsyncIdContainer(new CookieIdContainer[Id])

  override def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = Future
    .successful {
    print(user)
    (user.role, authority) match {
      case (Super, _) => true
      case (Admin, Admin) => true
      case (Admin, User) => true
      case (User, User) => true
      case _ => false
    }
  }

  override lazy val tokenAccessor = new CookieTokenAccessor(

    cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
    cookieMaxAge = Some(sessionTimeoutInSeconds)
  )

}
