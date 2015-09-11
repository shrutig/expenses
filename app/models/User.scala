package models

import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.Play.current

case class User(name: String, password: String) {
  def checkPassword(password: String): Boolean = {
    if (BCrypt.checkpw(password, this.password)) return true
    else false
  }
}


case class PasswordUpdateForm(currentPass: String, newPassword: String, repeatPassword: String) {
  val PASSWORD = "password"
  def updatePassword(userName: String): Boolean = {
    var status: Boolean = false
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(models.sqlStatement.USER_STATE_1)
      stmt.setString(1, userName)
      val rs = stmt.executeQuery()
      if (rs.next()) {
        val passRealHash = rs.getString(PASSWORD)
        if (newPassword == repeatPassword && BCrypt.checkpw(currentPass, passRealHash)) {
          val stmt1 = conn.prepareStatement(sqlStatement.USER_STATE_2)
          val newPassHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
          stmt1.setString(1, newPassHash)
          stmt1.setString(2, userName)
          stmt1.execute()
          status = true
        }
      }
    }
    status
  }
}

