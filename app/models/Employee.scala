package models


case class Employee(name: String, userName: String, password: String, accountNo: Int,
                    phone: Option[Int], email: String, address: Option[String], role: String)

case class UpdateEmployee(accountNo: Int, phone: Option[Int], email: String, address: Option[String])




