package org.scalarelational.manual.mapper

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait User {
  def name: String
  def id: Option[Int]
}