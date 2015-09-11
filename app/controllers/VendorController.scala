package controllers

import java.sql.Statement

import models.{VendorName, Vendor}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.DB
import play.api.Play.current
import play.api.mvc.{Action, Controller}
import views.html.helper

import scala.collection.mutable.ListBuffer

object VendorController extends Controller {

  private val VENDOR = "vendor"
  private val NAME = "name"
  private val PHONE = "phone"
  private val ADDRESS = "address"
  private val DESCRIPTION = "description"
  private val ACCOUNT_NO = "accountNo"
  private val BANK_DETAIL = "bankDetail"

  def vendor = Action { implicit request =>
    Ok(views.html.addVendor(""))
  }

  val vendorForm = Form(mapping(NAME -> text(maxLength = 20), PHONE -> number(min = 0), ACCOUNT_NO -> number(min = 0),
    BANK_DETAIL -> text(maxLength = 30), ADDRESS -> text(maxLength = 20), DESCRIPTION -> text(maxLength = 30))(Vendor
    .apply)
    (Vendor
    .unapply))

  def addVendor = Action(parse.form(vendorForm, onErrors = (withError: Form[Vendor]) =>
    Redirect("/vendor"))) { implicit request =>
    val vendor = request.body
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_1)
      stmt.setString(1, vendor.name)
      stmt.setInt(2, vendor.phone)
      stmt.setInt(3, vendor.accountNo)
      stmt.setString(4, vendor.bankDetail)
      stmt.setString(5, vendor.address)
      stmt.setString(6, vendor.description)
      stmt.executeUpdate()
    }
    Ok(views.html.addVendor(s"Vendor Information of ${vendor.name}  Added"))
  }

  def viewDeleteVendor = Action { implicit request =>
    Ok(views.html.deleteVendor(getListVendors.toList, ""))
  }

  def getListVendors: ListBuffer[models.Vendor] = {
    var vendorList = new ListBuffer[models.Vendor]
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_2)
      val rs = stmt.executeQuery()
      while (rs.next())
        vendorList += Vendor(rs.getString(NAME), rs.getInt(PHONE), rs.getInt(ACCOUNT_NO), rs.getString(BANK_DETAIL), rs
          .getString(ADDRESS), rs.getString(DESCRIPTION))
    }
    vendorList
  }

  def getVendorNameList: ListBuffer[String] = {
    var vendorList = new ListBuffer[String]
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_3)
      val rs = stmt.executeQuery()
      while (rs.next())
        vendorList += rs.getString(NAME)
    }
    vendorList
  }

  val deleteVendorForm = Form(mapping(VENDOR -> text)(VendorName.apply)(VendorName.unapply))

  def deleteVendor = Action(parse.form(deleteVendorForm, onErrors = (withError: Form[VendorName]) =>
    Redirect("/deleteVendor"))) { implicit request =>
    val vendorName = request.body.name
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_4)
      stmt.setString(1, vendorName)
      stmt.execute()
    }
    Ok(views.html.deleteVendor(getListVendors.toList, s"Vendor ${vendorName} deleted"))
  }

}
