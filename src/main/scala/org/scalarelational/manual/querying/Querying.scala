package org.scalarelational.manual.querying

import pl.metastack.metadocs.SectionSupport
import org.scalarelational.manual.mapper._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Querying extends SectionSupport {
  section("basicCoffees") {
    import MapperDatastore._
    import coffees._

    session {
      val query = select (*) from coffees
      query.result.toList.map { r =>
        s"${r(name)}\t${r(supID)}\t${r(price)}\t${r(sales)}\t${r(total)}"
      }
    }.mkString("\n")
  }

  val query = section("joinQuery") {
    import MapperDatastore._

    (
      select
        (coffees.name, suppliers.name)
      from
        coffees
      innerJoin
        suppliers
      on
        coffees.supID === suppliers.ref
      where
        coffees.price < 9.0
    )
  }

  section("join") {
    import MapperDatastore._

    session {
      query.result.toList.map { r =>
        s"Coffee: ${r(coffees.name)}, Supplier: ${r(suppliers.name)}"
      }.mkString("\n")
    }
  }

  section("tuple") {
    import MapperDatastore._

    session {
      query.result.toList.map { r =>
        val (coffeeName, supplierName) = r()
        s"Coffee: $coffeeName, Supplier: $supplierName"
      }
    }
  }

  section("update") {
    import MapperDatastore._
    import coffees._

    session {
      val query = update(name("updated name")) where id === Some(1)
      query.result
    }
  }

  section("delete") {
    import MapperDatastore._

    session {
      val query = delete(coffees) where coffees.id === Some(1)
      query.result
    }
  }

  section("functions") {
    import MapperDatastore._
    import coffees._

    session {
      val query = select(price.min, price.max) from coffees
      val (min, max) = query.result.one()
      s"Min Price: $min, Max Price: $max"
    }
  }
}