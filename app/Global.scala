
import play.api.Play.current
import play.api.Application
import play.api.GlobalSettings
import play.api.db.DB



object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val START_STATE_1= "select count(*) from employee where role = 'super';"
    val START_STATE_2= "INSERT INTO employee VALUES('','super');"
    val START_STATE_3= "update employee set role='super' where userName='';"
    val START_STATE_4 ="select count(*) from employee where userName='';"
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(START_STATE_1)
      val rs = stmt.executeQuery()
      val stmt1 = conn.prepareStatement(START_STATE_4)
      val rs1 = stmt1.executeQuery()
      rs.next()
      rs1.next()
      if (rs.getInt(1) == 0) {
        if(rs1.getInt(1) == 0) {
          val stmt2 = conn.prepareStatement(START_STATE_2)
          stmt2.executeUpdate()
        }
        else{
          val stmt2 = conn.prepareStatement(START_STATE_3)
          stmt2.executeUpdate()
        }
      }
    }
    super.onStart(app)
  }

}
