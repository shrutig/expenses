package models

sealed trait Role

object Role {

  case object Admin extends Role

  case object Super extends Role

  case object User extends Role

  def valueOf(value: String): Role = value match {
    case "admin" => Admin
    case "super" => Super
    case "user" => User
    case _ => throw new IllegalArgumentException()
  }

  def toString(role: Role) = role match {
    case Admin => "admin"
    case Super => "super"
    case User => "user"
  }

}