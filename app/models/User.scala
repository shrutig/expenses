package models

case class User(name: String, password: String) {
  def checkPassword(password: String): Boolean = {
    if (this.password == password) return true
    else false
  }
}

case class PasswordUpdate(currentPass: String, newPassword: String, repeatPassword: String)

