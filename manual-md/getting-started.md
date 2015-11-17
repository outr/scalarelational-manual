Previous chapter: [Introduction](introduction.md)  |  Next chapter: [Mapper](mapper.md)  |  [Edit source](https://github.com/outr/scalarelational-manual/edit/master/manual/getting_started.md)

# Getting started

This chapter will guide you through creating your first project with ScalaRelational. For the sake of simplicity we will use an in-memory H2 database.

## sbt dependencies
The first thing you need to do is add ScalaRelational’s H2 module to your sbt project:

```scala
libraryDependencies += "org.scalarelational" %% "scalarelational-h2" % "1.1.0"
```
If you’d prefer to use another database instead, please refer to the chapter [Databases](databases.md).


## Library imports
You will need the following imports:

```scala
import org.scalarelational.column.property.{AutoIncrement, ForeignKey, PrimaryKey, Unique}
import org.scalarelational.h2.{H2Datastore, H2Memory}
import org.scalarelational.table.Table
```



## Schema
The next thing you need is the database representation in Scala. The schema can map to an existing database or you can use it to create the tables in your database:

```scala
object GettingStartedDatastore extends H2Datastore(mode = H2Memory("getting_started")) {
  object suppliers extends Table("SUPPLIERS") {
    val name = column[String]("SUP_NAME", Unique)
    val street = column[String]("STREET")
    val city = column[String]("CITY")
    val state = column[String]("STATE")
    val zip = column[String]("ZIP")
    val id = column[Int]("SUP_ID", PrimaryKey, AutoIncrement)
  }

  object coffees extends Table("COFFEES") {
    val name = column[String]("COF_NAME", Unique)
    val supID = column[Int]("SUP_ID", new ForeignKey(suppliers.id))
    val price = column[Double]("PRICE")
    val sales = column[Int]("SALES")
    val total = column[Int]("TOTAL")
    val id = column[Int]("COF_ID", PrimaryKey, AutoIncrement)
  }
}
```


Our `Datastore` contains `Table`s and our `Table`s contain `Column`s. As for the `Datastore` we have chosen an in-memory H2 database. Every column type must have a `DataType` associated with it. You don’t see it referenced above because all standard Scala types have predefined implicit conversions available [^1]. If you need to use a type that is not supported by ScalaRelational, please refer to [Defining a custom type](table-definition-1.md#defining-a-custom-type).


## Create the database
Now that we have our schema defined in Scala, we need to create the tables in the database:

```scala
import GettingStartedDatastore._

session {
  create(suppliers, coffees)
}
```

**Output:**
```
3
```

All database queries must take place within a *session*.

### Import
You’ll notice we imported `ExampleDatastore.` in an effort to minimise the amount of code required here. We can explicitly write it more verbosely like this:

```scala
GettingStartedDatastore.session {
  GettingStartedDatastore.create(
    GettingStartedDatastore.suppliers,
    GettingStartedDatastore.coffees
  )
}
```


For the sake of readability importing the datastore is generally suggested. Although if namespace collisions are a problem you can import and alias or create a shorter reference like this:

```scala
def ds = GettingStartedDatastore

ds.session {
  ds.create(ds.suppliers, ds.coffees)
}
```




## Inserting
ScalaRelational supports type-safe insertions:

```scala
import GettingStartedDatastore._
import suppliers._

session {
  acmeId = insert(name("Acme, Inc."), street("99 Market Street"), city("Groundsville"), state("CA"), zip("95199")).result
  superiorCoffeeId = insert(id(49), name("Superior Coffee"), street("1 Party Place"), city("Mendocino"), state("CA"), zip("95460")).result
}
```


If we don’t call `result`, we will just create the query without ever executing it. Please note that `result` must be called within the session.

There is also a shorthand when using values in order:

```scala
import GettingStartedDatastore._

session {
  theHighGroundId = insertInto(suppliers, 150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966").result
}
```


The database returns -1 as the ID is already known.

If you want to insert multiple rows at the same time, you can use a batch insertion:

```scala
import GettingStartedDatastore._
import coffees._

session {
  insert(name("Colombian"), supID(acmeId), price(7.99), sales(0), total(0)).
    and(name("French Roast"), supID(superiorCoffeeId), price(8.99), sales(0), total(0)).
    and(name("Espresso"), supID(theHighGroundId), price(9.99), sales(0), total(0)).
    and(name("Colombian Decaf"), supID(acmeId), price(8.99), sales(0), total(0)).
    and(name("French Roast Decaf"), supID(superiorCoffeeId), price(9.99), sales(0), total(0)).result
}
```

**Output:**
```
List(5)
```

This is very similar to the previous insert method, except instead of calling `result` we’re calling `and`. This converts the insert into a batch insert and you gain the performance of being able to insert several records with one insert statement.

You can also pass a `Seq` to `insertBatch`, which is useful if the rows are loaded from a file for example:

```scala
import GettingStartedDatastore._
import coffees._

session {
  val rows = (0 to 10).map { index =>
    List(name(s"Generic Coffee ${index + 1}"), supID(49), price(6.99), sales(0), total(0))
  }
  insertBatch(rows).result
}
```

**Output:**
```
List(16)
```


## Querying
The DSL for querying a table is similar to SQL:

```scala
import GettingStartedDatastore._
import coffees._

session {
  val query = select (*) from coffees

  query.result.map { r =>
    s"${r(name)}\t${r(supID)}\t${r(price)}\t${r(sales)}\t${r(total)}"
  }.mkString("\n")
}
```

**Output:**
```
Colombian	1	7.99	0	0
French Roast	49	8.99	0	0
Espresso	93966	9.99	0	0
Colombian Decaf	1	8.99	0	0
French Roast Decaf	49	9.99	0	0
Generic Coffee 1	49	6.99	0	0
Generic Coffee 2	49	6.99	0	0
Generic Coffee 3	49	6.99	0	0
Generic Coffee 4	49	6.99	0	0
Generic Coffee 5	49	6.99	0	0
Generic Coffee 6	49	6.99	0	0
Generic Coffee 7	49	6.99	0	0
Generic Coffee 8	49	6.99	0	0
Generic Coffee 9	49	6.99	0	0
Generic Coffee 10	49	6.99	0	0
Generic Coffee 11	49	6.99	0	0
```

Although that could look a little prettier by explicitly querying what we want to see:

```scala
import GettingStartedDatastore.{coffees => c, _}

session {
  val query = select (c.name, c.supID, c.price, c.sales, c.total) from c

  query.result.converted.map {
    case (name, supID, price, sales, total) => s"$name  $supID  $price  $sales  $total"
  }.mkString("\n")
}
```

**Output:**
```
Colombian  1  7.99  0  0
French Roast  49  8.99  0  0
Espresso  93966  9.99  0  0
Colombian Decaf  1  8.99  0  0
French Roast Decaf  49  9.99  0  0
Generic Coffee 1  49  6.99  0  0
Generic Coffee 2  49  6.99  0  0
Generic Coffee 3  49  6.99  0  0
Generic Coffee 4  49  6.99  0  0
Generic Coffee 5  49  6.99  0  0
Generic Coffee 6  49  6.99  0  0
Generic Coffee 7  49  6.99  0  0
Generic Coffee 8  49  6.99  0  0
Generic Coffee 9  49  6.99  0  0
Generic Coffee 10  49  6.99  0  0
Generic Coffee 11  49  6.99  0  0
```

Joins are supported too. In the following example we query all coffees back filtering and joining with suppliers:

```scala
import GettingStartedDatastore._

session {
  val query = (select(coffees.name, suppliers.name)
    from coffees
    innerJoin suppliers on coffees.supID === suppliers.id
    where coffees.price < 9.0)

  query.result.map { r =>
    s"Coffee: ${r(coffees.name)}, Supplier: ${r(suppliers.name)}"
  }.mkString("\n")
}
```

**Output:**
```
Coffee: Colombian, Supplier: Acme, Inc.
Coffee: French Roast, Supplier: Superior Coffee
Coffee: Colombian Decaf, Supplier: Acme, Inc.
Coffee: Generic Coffee 1, Supplier: Superior Coffee
Coffee: Generic Coffee 2, Supplier: Superior Coffee
Coffee: Generic Coffee 3, Supplier: Superior Coffee
Coffee: Generic Coffee 4, Supplier: Superior Coffee
Coffee: Generic Coffee 5, Supplier: Superior Coffee
Coffee: Generic Coffee 6, Supplier: Superior Coffee
Coffee: Generic Coffee 7, Supplier: Superior Coffee
Coffee: Generic Coffee 8, Supplier: Superior Coffee
Coffee: Generic Coffee 9, Supplier: Superior Coffee
Coffee: Generic Coffee 10, Supplier: Superior Coffee
Coffee: Generic Coffee 11, Supplier: Superior Coffee
```


## Remarks
You may have noticed the striking similarity between this code and Slick’s introductory tutorial. This was done purposefully to allow better comparison of functionality between the two frameworks.

An auto-incrementing ID has been introduced into both tables to better represent the standard development scenario. Rarely do you have external IDs to supply to your database like [Slick represents](http://slick.typesafe.com/doc/3.0.0/gettingstarted.html#schema).



[^1]: See the [`DataTypeSupport` trait](https://github.com/outr/scalarelational/blob/master/core/src/main/scala/org/scalarelational/datatype/DataTypeSupport.scala) for more information