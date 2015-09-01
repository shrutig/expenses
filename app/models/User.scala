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
  def updatePassword(userName: String) = {

    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("select password from employee where username=\"" + userName + "\";")
      if (rs.next()) {
        val currentPass = rs.getString("password")
        if (newPassword == repeatPassword)
          stmt.execute("update employee set password=\"" + newPassword +
            "\" where username=\"" + userName + "\";")
      }
    }
    finally conn.close()
  }
}

