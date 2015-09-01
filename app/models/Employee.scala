package models

import anorm._
import play.api.db.DB
import play.api.Play.current

case class Employee(name: String, userName: String, password: String, accountNo: Int,
                    phone: Int, email: String, address: String, role: String) {
  def addEmployee = {
    /*
    DB.withConnection { implicit connection =>
      SQL("insert into employee(name,username,password,accountNo,phone,email,address,role) values " +
        "({name},{username},{password},{accountNo},{phone},{email},{address},{role})").on(
          'name -> employee.name, 'username -> employee.userName, 'password -> employee.password,
          'accountNo -> employee.accountNo, 'phone -> employee.phone,
          'email -> employee.email, 'address -> employee.address)
    }
    */
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("insert into employee(name,username,password,accountNo,phone,email,address,role) values (\"" +
        name + "\",\"" + userName + "\",\"" + password + "\"," + accountNo + ","
        + phone + ",\"" + email + "\",\"" + address + "\",\"" + role + "\");")
    }
    finally {
      conn.close()
    }
  }
}

case class UpdateProfileForm(accountNo: Int, phone: Int, email: String, address: String){
  def update(userName:String)={
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement()
      stmt.execute("update employee set accountNo=" + accountNo + ",phone=" + phone + ",email=\"" +
        email + "\",address=\"" + address + "\" where username=\"" + userName + "\";")
    }
    finally {
      conn.close()
    }
  }
}

case class EmployeeName(username: String)







