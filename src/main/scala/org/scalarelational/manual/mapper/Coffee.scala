package org.scalarelational.manual.mapper

import org.scalarelational.datatype.Ref
import org.scalarelational.mapper.Entity

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class Coffee(name: String,
                  supID: Ref[Supplier],
                  price: Double,
                  sales: Int,
                  total: Int,
                  id: Option[Int] = None) extends Entity[Coffee] {
  def columns = mapTo[Coffee](MapperDatastore.coffees)
}
