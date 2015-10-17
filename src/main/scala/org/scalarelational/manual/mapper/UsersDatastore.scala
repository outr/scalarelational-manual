package org.scalarelational.manual.mapper

import org.scalarelational.column.property.{Polymorphic, AutoIncrement, PrimaryKey}
import org.scalarelational.h2.{H2Memory, H2Datastore}
import org.scalarelational.mapper._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object UsersDatastore extends H2Datastore(mode = H2Memory("mapper")) {
  object users extends MappedTable[User]("users") {
    val id = column[Option[Int], Int]("id", PrimaryKey, AutoIncrement)
    val name = column[String]("name")
    val canDelete = column[Boolean]("canDelete", Polymorphic)
    val isGuest = column[Boolean]("isGuest")

    override def query = q.to[User]
  }
}
