package models

object sqlStatement {
  val PAY_STATE_1 = "insert into expenses (userName,vendor,amount,status,description) values(?,?,?,?,?);"
  val PAY_STATE_2 = "update expenses set status=? where id=?;"
  val PAY_STATE_3 = "update expenses set status=?,admin=? where id=?;"
  val PAY_STATE_4 = "update expenses set status=? , fileName=? where id=? ;"
  val PAY_STATE_5 = "select * from expenses where status=?;"
  val PAY_STATE_6 = "select accountNo,bankDetail from vendor where name=?;"
  val VENDOR_STATE_1 = "insert into vendor values(?,?,?,?,?,?);"
  val VENDOR_STATE_2 = "select name,phone,accountNo,bankDetail,address,description from vendor;"
  val VENDOR_STATE_3 = "select name from vendor;"
  val VENDOR_STATE_4 = "delete from vendor where name=?;"
  val VENDOR_STATE_5 = "select count(*) from vendor where name=?"
  val VENDOR_STATE_6 = "select count(*) from expenses where vendor=? and (status<>'P');"
  val EMP_STATE_1 = "insert into employee values (?,?);"
  val EMP_STATE_2 = "select role from employee where userName=?"
  val EMP_STATE_3 = "update employee set role=? where userName=?;"
  val EMP_STATE_4 = "select * from employee"
}
