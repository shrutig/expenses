package controllers

import models.{VendorName, Vendor}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.Play.current
import play.api.mvc.{Action, Controller}
import views.html.helper

import scala.collection.mutable.ListBuffer

object VendorController extends Controller {

  def vendor = Action { implicit request =>
     Ok(views.html.addVendor(""))
  }

  val vendorForm = Form(mapping("name" -> text, "phone" -> number(min = 0),
    "address" -> text, "description" -> text)(Vendor.apply)(Vendor.unapply))

  def addVendor = Action(parse.form(vendorForm, onErrors = (withError: Form[Vendor]) =>
    Redirect("/vendor"))) { implicit request =>
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

    Ok(views.html.addVendor("Vendor Information of "+vendor.name+" Added"))
  }

  def viewDeleteVendor = Action { implicit request =>
      Ok(views.html.deleteVendor(getListVendors.toList,""))
  }

  def getListVendors:ListBuffer[Vendor] = {
    var vendorList = new ListBuffer[Vendor]
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("select name,phone,address,description from vendor;")
      while (rs.next())
        vendorList += Vendor(rs.getString("name"),rs.getInt("phone"),
          rs.getString("address"),rs.getString("description"))
    }
    finally conn.close()
    vendorList
  }

  val deleteVendorForm = Form(mapping("vendor" -> text)(VendorName.apply)(VendorName.unapply))

  def deleteVendor = Action(parse.form(deleteVendorForm, onErrors = (withError: Form[VendorName]) =>
    Redirect("/deleteVendor"))) { implicit request =>
    val vendorName = request.body.name
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("delete from vendor where name=\"" + vendorName + "\";")
    } finally conn.close()
    Ok(views.html.deleteVendor(getListVendors.toList,"Vendor "+vendorName+" deleted"))
  }

}
