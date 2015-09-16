package models

case class User(userName: String, password: String) {
}


case class PasswordUpdateForm(currentPass: String, newPassword: String, repeatPassword: String) {

}

