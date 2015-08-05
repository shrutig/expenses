package controllers

import models.Vendor
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.Play.current
import play.api.mvc.{Action, Controller}

object VendorController extends Controller {

  def vendor = Action { implicit request =>
    val userType = request.session("userType")
    if ((userType == "admin") || (userType == "super")) Ok(views.html.vendor(""))
    else Redirect("/")
  }

  val vendorForm = Form(mapping("name" -> text, "phone" -> number,
    "address" -> text)(Vendor.apply)(Vendor.unapply))

  def addVendor = Action(parse.form(vendorForm, onErrors = (withError: Form[Vendor]) =>
    BadRequest("/vendor"))) { implicit request =>
    val vendor = request.body
    /* DB.withConnection { implicit connection =>
       SQL("INSERT INTO vendor (name,phone,address) VALUES ({name},{phone},{address});").on("name" -> vendor.name,
         "phone" -> vendor.phone, "address" -> vendor.address).executeUpdate()
     }*/

    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      stmt.execute("insert into vendor (name,phone,address) values (\"" + vendor.name +
        "\"," + vendor.phone + ",\"" + vendor.address + "\");")
    }
    finally {
      conn.close()
    }
    Ok(views.html.vendor(""))
  }

  def listVendor = Action {
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
    }
    finally {
      conn.close()
    }
    Ok("")
  }

}
