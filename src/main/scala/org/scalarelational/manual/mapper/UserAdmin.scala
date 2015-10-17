package org.scalarelational.manual.mapper

import org.scalarelational.mapper.Entity

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class UserAdmin(name: String, canDelete: Boolean, id: Option[Int] = None) extends User with Entity[UserAdmin] {
  def columns = mapTo[UserAdmin](UsersDatastore.users)

  val isGuest = false
}