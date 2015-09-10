package models

import play.api.db.DB
import play.api.Play.current

case class User(name: String, password: String) {
  def checkPassword(password: String): Boolean = {
    if (this.password == password) return true
    else false
  }
}

case class PasswordUpdateForm(currentPass: String, newPassword: String, repeatPassword: String) {
  def updatePassword(userName: String):Boolean = {
   var status: Boolean = false
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("select password from employee where username=\"" + userName + "\";")
      if (rs.next()) {
        val currentPassReal = rs.getString("password")
        if (newPassword == repeatPassword && currentPass == currentPassReal) {
          stmt.execute("update employee set password=\"" + newPassword +
            "\" where username=\"" + userName + "\";")
          status = true
        }
      }
    }
    finally conn.close()
    status
  }
}

