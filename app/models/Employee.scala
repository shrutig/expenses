package models

case class Employee(name: String, userName: String, password: String, accountNo: Int,
                    phone: Int, email: String, address: String, role: String) {

}

case class UpdateProfileForm(accountNo: Int, phone: Int, email: String, address: String){
}

case class EmployeeName(userName: String)







