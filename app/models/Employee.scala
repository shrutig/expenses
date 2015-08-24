package models

import anorm._
import play.api.db.DB


case class Employee(name: String, userName: String, password: String, accountNo: Int,
                    phone: Int, email: String, address: String, role: String)

case class UpdateEmployee(accountNo: Int, phone: Int, email: String, address: String)

case class EmployeeName(username: String)







