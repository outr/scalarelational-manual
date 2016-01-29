package org.scalarelational.manual.mapper

import pl.metastack.metadocs.SectionSupport
import org.scalarelational.instruction.InsertSingle
import org.scalarelational.mapper._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Mapper extends SectionSupport {
  section("create") {
    import MapperDatastore._

    withSession { implicit session =>
      create(suppliers, coffees)
    }
  }

  section("insertSupplier") {
    import MapperDatastore._

    withSession { implicit session =>
      val starbucks = Supplier("Starbucks", "123 Everywhere Rd.", "Lotsaplaces", Some("CA"), "93966")
      starbucks.insert.result
    }
  }

  section("insertSuppliers") {
    import MapperDatastore._
    import Ids._

    transaction { implicit session =>
      acmeId = Supplier("Acme, Inc.", "99 Market Street", "Groundsville", Some("CA"), "95199").insert.result
      superiorCoffeeId = Supplier("Superior Coffee", "1 Party Place", "Mendocino", None, "95460").insert.result
      theHighGroundId = Supplier("The High Ground", "100 Coffee Lane", "Meadows", Some("CA"), "93966").insert.result

      (acmeId, superiorCoffeeId, theHighGroundId)
    }
  }

  section("insertBatch") {
    import MapperDatastore._
    import Ids._

    withSession { implicit session =>
      Coffee("Colombian", acmeId, 7.99, 0, 0).insert.
        and(Coffee("French Roast", superiorCoffeeId, 8.99, 0, 0).insert).
        and(Coffee("Espresso", theHighGroundId, 9.99, 0, 0).insert).
        and(Coffee("Colombian Decaf", acmeId, 8.99, 0, 0).insert).
        and(Coffee("French Roast Decaf", superiorCoffeeId, 9.99, 0, 0).insert).result
    }
  }

  section("queryBasic") {
    import MapperDatastore._
    import suppliers._

    withSession { implicit session =>
      val query = select (*) from suppliers where name === "Starbucks"
      query.to[Supplier](suppliers).result.head
    }
  }

  section("queryRefs") {
    import MapperDatastore._

    withSession { implicit session =>
      val query = (
        select (coffees.name, suppliers.name)
          from coffees
          innerJoin suppliers
          on coffees.supID === suppliers.ref
        )
      query.result.toList.mkString("\n")
    }
  }

  section("queryJoins") {
    import MapperDatastore._

    withSession { implicit session =>
      val query = (
        select (coffees.* ::: suppliers.*)
          from coffees
          innerJoin suppliers
          on (coffees.supID === suppliers.ref)
          where coffees.name === "French Roast"
        )

      val (frenchRoast, superior) = query.to[Coffee, Supplier](coffees, suppliers).converted.head
      s"Coffee: $frenchRoast\nSupplier: $superior"
    }
  }

  section("userCreate") {
    import UsersDatastore._

    withSession { implicit session =>
      create(users)
    }
  }

  section("usersInsert") {
    import UsersDatastore._

    withSession { implicit session =>
      UserGuest("guest").insert.result
      UserAdmin("admin", canDelete = true).insert.result
    }
  }

  section("usersQuery") {
    import UsersDatastore._
    val query = users.query

    withSession { implicit session =>
      query.result.toList.mkString("\n")
    }
  }
}