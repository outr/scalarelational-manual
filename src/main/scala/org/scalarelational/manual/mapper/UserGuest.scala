package org.scalarelational.manual.mapper

import org.scalarelational.mapper.Entity

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class UserGuest(name: String, id: Option[Int] = None) extends User with Entity[UserGuest] {
  def columns = mapTo[UserGuest](UsersDatastore.users)

  val isGuest = true
}