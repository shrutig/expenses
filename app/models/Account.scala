package models

import play.api.Play.current
import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB

import scala.collection.mutable.ListBuffer


case class Account(id: String, password: String, name: String, accountNo: Int, phone: Int
                   , email: String, address: String, role: Role)

object Account {
  private val NAME = "name"
  private val USER_NAME = "userName"
  private val PASSWORD = "password"
  private val ACCOUNT_NO = "accountNo"
  private val PHONE = "phone"
  private val EMAIL = "email"
  private val ADDRESS = "address"
  private val ROLE = "role"

  def authenticate(id: String, password: String): Option[Account] = {
   findById(id).filter { account => BCrypt.checkpw(password, account.password) }
  }

  def findById(id: String): Option[Account] = {
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.ACCOUNT_STATE_2)
      stmt.setString(1, id)
      val rs = stmt.executeQuery()
      var a:Option[Account]= None
      if (rs.next()) {
        a =Some( Account(rs.getString(USER_NAME), rs.getString(PASSWORD), rs.getString(NAME), rs.getInt(ACCOUNT_NO), rs
          .getInt
          (PHONE), rs.getString(EMAIL), rs.getString(ADDRESS), Role.valueOf(rs.getString(ROLE))))
      }
      a
    }
  }

  def findAll(): Seq[Account] = {
    val seqAcc = new ListBuffer[Account]
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_CONST_STATE_2)
      val rs = stmt.executeQuery()
      while (rs.next()) {
        print(rs.getString(ROLE))
        seqAcc += Account(rs.getString(USER_NAME), rs.getString(PASSWORD), rs.getString(NAME), rs.getInt(ACCOUNT_NO), rs
          .getInt
          (PHONE), rs.getString(EMAIL), rs.getString(ADDRESS), Role.valueOf(rs.getString(ROLE)))
      }
    }
    seqAcc
  }

  def create(account: Account) {
    val pass = BCrypt.hashpw(account.password, BCrypt.gensalt())
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_STATE_1)
      stmt.setString(1, account.id)
      stmt.setString(2, pass)
      stmt.setString(3, account.name)
      stmt.setInt(4, account.accountNo)
      stmt.setInt(5, account.phone)
      stmt.setString(6, account.email)
      stmt.setString(7, account.address)
      stmt.setString(8, Role.toString(account.role))
      stmt.executeUpdate()
    }
  }

  def deleteAccount(id: String) = {
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.EMP_CONST_STATE_3)
      stmt.setString(1, id)
      stmt.execute()
    }
  }

  def updateAccount(id:String,form:UpdateProfileForm)={
    DB.withConnection{ conn =>
      val stmt = conn.prepareStatement(sqlStatement.EMP_STATE_2)
      stmt.setInt(1,form.accountNo)
      stmt.setInt(2,form.phone)
      stmt.setString(3,form.email)
      stmt.setString(4,form.address)
      stmt.setString(5,id)
      stmt.execute()
    }
  }

  def updatePassword(id:String,form: PasswordUpdateForm):Boolean={
    val PASSWORD = "password"
      var status: Boolean = false
      DB.withConnection { conn =>
        val stmt = conn.prepareStatement(models.sqlStatement.USER_STATE_1)
        stmt.setString(1, id)
        val rs = stmt.executeQuery()
        if (rs.next()) {
          val passRealHash = rs.getString(PASSWORD)
          if (form.newPassword == form.repeatPassword && BCrypt.checkpw(form.currentPass, passRealHash)) {
            val stmt1 = conn.prepareStatement(sqlStatement.USER_STATE_2)
            val newPassHash = BCrypt.hashpw(form.newPassword, BCrypt.gensalt())
            stmt1.setString(1, newPassHash)
            stmt1.setString(2, id)
            stmt1.execute()
            status = true
          }
        }
      }
      status
  }

}