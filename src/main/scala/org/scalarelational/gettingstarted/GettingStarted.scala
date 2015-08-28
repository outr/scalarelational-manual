package org.scalarelational.gettingstarted

/**
 * @author Matt Hicks <matt@outr.com>
 */
object GettingStarted extends App {
  import ExampleDatastore._

  session {
    create(suppliers, coffees)
  }

  {
  /* <InsertSuppliers> */
    import suppliers._

    session {
      insert(id(101), name("Acme, Inc."), street("99 Market Street"),
        city("Groundsville"), state("CA"), zip("95199")).result
      insert(id(49), name("Superior Coffee"), street("1 Party Place"),
        city("Mendocino"), state("CA"), zip("95460")).result
    }
  /* </InsertSuppliers> */
  }

  {
  /* <QueryCoffees> */
    import coffees._

    session {
      val query = select(*) from coffees

      query.result.map { r =>
        s"${r(name)}\t${r(supID)}\t${r(price)}\t${r(sales)}\t${r(total)}"
      }.mkString("\n")
    }
  /* </QueryCoffees> */
  }
}
