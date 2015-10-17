package org.scalarelational.manual.mapper

import org.scalarelational.column.property.{AutoIncrement, ForeignKey, PrimaryKey, Unique}
import org.scalarelational.datatype.Ref
import org.scalarelational.h2.{H2Datastore, H2Memory}
import org.scalarelational.mapper._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object MapperDatastore extends H2Datastore(mode = H2Memory("mapper")) {
  object suppliers extends MappedTable[Supplier]("SUPPLIERS") {
    val name = column[String]("SUP_NAME", Unique)
    val street = column[String]("STREET")
    val city = column[String]("CITY")
    val state = column[Option[String], String]("STATE")
    val zip = column[String]("ZIP")
    val id = column[Option[Int], Int]("SUP_ID", PrimaryKey, AutoIncrement)

    override def query = q.to[Supplier]
  }

  object coffees extends MappedTable[Coffee]("COFFEES") {
    val name = column[String]("COF_NAME", Unique)
    val supID = column[Ref[Supplier], Int]("SUP_ID", new ForeignKey(suppliers.id))
    val price = column[Double]("PRICE")
    val sales = column[Int]("SALES")
    val total = column[Int]("TOTAL")
    val id = column[Option[Int], Int]("COF_ID", PrimaryKey, AutoIncrement)

    override def query = q.to[Coffee]
  }
}