import play.api.GlobalSettings
import play.api.mvc.{Handler, RequestHeader}

object Global extends GlobalSettings {

/*
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {

    (request.method, request.path) match {
      case ("GET", "/") => super.onRouteRequest(request)
      case ("POST", "/authenticate") => super.onRouteRequest(request)
      case _ => {
        val userType = request.session("userType")
        if ((userType == "super") || (userType == "admin")) super.onRouteRequest(request)
        else Some(controllers.LoginController.home)
      }
    }
  }
*/

}

