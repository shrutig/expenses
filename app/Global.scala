import org.mindrot.jbcrypt.BCrypt
import play.api.Application
import play.api.db.DB
import org.mindrot.jbcrypt.BCrypt
import play.api.Play.current
import play.api.Application
import play.api.GlobalSettings
import play.api.db.DB
import play.api.mvc.{Handler, RequestHeader}
import play.api.mvc.Results._
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import play.api.i18n.{ Messages, Lang }



object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    val START_STATE_1= "select count(*) from employee where role = 'super';"
    val START_STATE_2= "INSERT INTO employee VALUES('shruti.gumma@tuplejump.com','super');"
    val START_STATE_3= "update employee set role='super' where userName='shruti.gumma@tuplejump.com';"
    DB.withConnection { conn =>
      val stmt = conn.prepareStatement(START_STATE_1)
      val rs = stmt.executeQuery()
      rs.next()
      if (rs.getInt(1) == 0) {
        val stmt1 = conn.prepareStatement(START_STATE_2)
        stmt1.executeUpdate()
      }
    }
    super.onStart(app)
  }

}
