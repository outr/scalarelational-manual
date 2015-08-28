package org.scalarelational.gettingstarted

import org.scalarelational.column.property._
import org.scalarelational.h2.{H2Datastore, H2Memory}
import org.scalarelational.table.Table

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ExampleDatastore extends H2Datastore(mode = H2Memory("example")) {
  object suppliers extends Table("SUPPLIERS") {
    val id = column[Int]("SUP_ID", PrimaryKey)
    val name = column[String]("SUP_NAME")
    val street = column[String]("STREET")
    val city = column[String]("CITY")
    val state = column[String]("STATE")
    val zip = column[String]("ZIP")
  }

  object coffees extends Table("COFFEES") {
    val name = column[String]("COF_NAME", PrimaryKey)
    val supID = column[Int]("SUP_ID", new ForeignKey(ExampleDatastore.suppliers.id))
    val price = column[Double]("PRICE")
    val sales = column[Int]("SALES")
    val rating = column[Option[Double], Double]("RATING")
    val total = column[Int]("TOTAL")
  }
}