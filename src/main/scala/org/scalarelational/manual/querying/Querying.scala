package org.scalarelational.manual.querying

import pl.metastack.metadocs.SectionSupport
import org.scalarelational.manual.mapper._

object Querying extends SectionSupport {
  section("basicCoffees") {
    import MapperDatastore._
    import coffees._

    withSession { implicit session =>
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

    withSession { implicit session =>
      query.result.toList.map { r =>
        s"Coffee: ${r(coffees.name)}, Supplier: ${r(suppliers.name)}"
      }.mkString("\n")
    }
  }

  section("tuple") {
    import MapperDatastore._

    withSession { implicit session =>
      query.converted.toList.map {
        case (coffeeName, supplierName) => s"Coffee: $coffeeName, Supplier: $supplierName"
      }
    }
  }

  section("update") {
    import MapperDatastore._
    import coffees._

    withSession { implicit session =>
      val query = update(name("updated name")) where id === Some(1)
      query.result
    }
  }

  section("delete") {
    import MapperDatastore._

    withSession { implicit session =>
      val query = delete(coffees) where coffees.id === Some(1)
      query.result
    }
  }

  section("functions") {
    import MapperDatastore._
    import coffees._

    withSession { implicit session =>
      val query = select(Min(price), Max(price)) from coffees
      val (min, max) = query.converted.one
      s"Min Price: $min, Max Price: $max"
    }
  }
}