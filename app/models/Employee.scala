package models

import org.mindrot.jbcrypt.BCrypt
import play.api.db.DB
import play.api.Play.current

case class Employee(name: String, userName: String, password: String, accountNo: Int,
                    phone: Int, email: String, address: String, role: String) {
  def addEmployee = {
    DB.withConnection {conn =>
      val stmt = conn.prepareStatement(sqlStatement.EMP_STATE_1)
      stmt.setString(1,userName)
      val passHash = BCrypt.hashpw(password, BCrypt.gensalt())
      stmt.setString(2,passHash)
      stmt.setString(3,name)
      stmt.setInt(4,accountNo)
      stmt.setInt(5,phone)
      stmt.setString(6,email)
      stmt.setString(7,address)
      stmt.setString(8,role)
      stmt.execute()
    }
  }
}

case class UpdateProfileForm(accountNo: Int, phone: Int, email: String, address: String){
  def update(userName:String)={
    DB.withConnection{ conn =>
      val stmt = conn.prepareStatement(sqlStatement.EMP_STATE_2)
      stmt.setInt(1,accountNo)
      stmt.setInt(2,phone)
      stmt.setString(3,email)
      stmt.setString(4,address)
      stmt.setString(5,userName)
      stmt.execute()
    }
  }
}

case class EmployeeName(userName: String)







