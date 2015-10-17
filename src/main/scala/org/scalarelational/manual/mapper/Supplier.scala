package org.scalarelational.manual.mapper

import org.scalarelational.mapper.Entity

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class Supplier(name: String,
                    street: String,
                    city: String,
                    state: Option[String],
                    zip: String,
                    id: Option[Int] = None) extends Entity[Supplier] {
  def columns = mapTo[Supplier](MapperDatastore.suppliers)
}