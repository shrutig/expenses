package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import models.authorizations.{Admin, WithRole}
import models.{User, VendorName, Vendor}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.MessagesApi
import play.api.db.DB
import play.api.Play.current
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

class VendorController @Inject()(
                                  val messagesApi: MessagesApi,
                                  val env: Environment[User, CookieAuthenticator],
                                  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User, CookieAuthenticator] {

  private val VENDOR = "vendor"
  private val NAME = "name"
  private val PHONE = "phone"
  private val ADDRESS = "address"
  private val DESCRIPTION = "description"
  private val ACCOUNT_NO = "accountNo"
  private val BANK_DETAIL = "bankDetail"

  def vendor = SecuredAction(WithRole(Admin) ) { implicit request =>
    Ok(views.html.addVendor(vendorForm, "", request.identity))
  }

  val vendorForm = Form(mapping(NAME -> text(maxLength = 20), PHONE -> number(min = 0), ACCOUNT_NO -> number(min = 0),
    BANK_DETAIL -> text(maxLength = 30), ADDRESS -> text(maxLength = 20), DESCRIPTION -> text(maxLength = 30))(Vendor
    .apply)(Vendor.unapply))

  def addVendor() = SecuredAction(WithRole(Admin) ).async { implicit request =>
    vendorForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.addVendor(vendorForm, "wrong data", request.identity))),
      data => {
        DB.withConnection { conn =>
          val stmt1 = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_5)
          stmt1.setString(1,data.name)
          val rs = stmt1.executeQuery()
          if(rs.next()){
            if(rs.getInt(1) == 0){
              val stmt = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_1)
              stmt.setString(1, data.name)
              stmt.setInt(2, data.phone)
              stmt.setInt(3, data.accountNo)
              stmt.setString(4, data.bankDetail)
              stmt.setString(5, data.address)
              stmt.setString(6, data.description)
              stmt.executeUpdate()
              Future.successful(Ok(views.html.addVendor(vendorForm, s"Vendor Information of ${data.name}  Added", request.identity)))
            }
            else
              Future.successful(BadRequest(views.html.addVendor(vendorForm, "vendor already exists", request.identity)))
          }
          else
            Future.successful(BadRequest(views.html.addVendor(vendorForm, "wrong data", request.identity)))
        }
      }
    )
  }


  def viewDeleteVendor = SecuredAction(WithRole(Admin) ).async { implicit request =>
    Future.successful(Ok(views.html.deleteVendor(getListVendors.toList, "", request.identity)))
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

  def deleteVendor() = SecuredAction(WithRole(Admin) ).async { implicit request =>
    deleteVendorForm.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.deleteVendor(getListVendors.toList, "wrong data", request.identity))),
      data => {
        DB.withConnection { conn =>
          val stmt = conn.prepareStatement(models.sqlStatement.VENDOR_STATE_4)
          stmt.setString(1, data.name)
          stmt.execute()
        }
        Future.successful(Ok(views.html.deleteVendor(getListVendors.toList, s"Vendor ${data.name} deleted", request
          .identity)))
      }
    )
  }


}
