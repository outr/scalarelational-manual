Previous chapter: [Getting started](getting-started.md)  |  Next chapter: [Querying](querying-1.md)  |  [Edit source](https://github.com/outr/scalarelational-manual/edit/master/manual/mapper.md)

# Mapper

The mapper module provides functionality to map table rows when persisting or selecting rows.

## sbt dependency
We must first add another dependency to our build file:

```scala
libraryDependencies += "org.scalarelational" %% "scalarelational-mapper" % "1.3.0"

libraryDependencies += "org.scalarelational" %% "scalarelational-h2" % "1.3.0"
```

## Library imports
For the mapper you need the following additional import:

```scala
import org.scalarelational.column.property.{AutoIncrement, ForeignKey, PrimaryKey, Unique}
import org.scalarelational.datatype.Ref
import org.scalarelational.h2.{H2Datastore, H2Memory}
import org.scalarelational.mapper._
```



## Table definition
When defining a table definition with the mapper, the key difference is that you need to use `MappedTable` and supply the `case class` you want to map it to. We change the example from the previous chapter to:

```scala
object MapperDatastore extends H2Datastore(mode = H2Memory("mapper")) {
  object suppliers extends MappedTable[Supplier]("SUPPLIERS") {
    val name = column[String]("SUP_NAME", Unique)
    val street = column[String]("STREET")
    val city = column[String]("CITY")
    val state = column[Option[String], String]("STATE")
    val zip = column[String]("ZIP")
    val id = column[Option[Int], Int]("SUP_ID", PrimaryKey, AutoIncrement)

    override def query = q.to[Supplier](suppliers)
  }

  object coffees extends MappedTable[Coffee]("COFFEES") {
    val name = column[String]("COF_NAME", Unique)
    val supID = column[Ref[Supplier], Int]("SUP_ID", new ForeignKey(suppliers.id))
    val price = column[Double]("PRICE")
    val sales = column[Int]("SALES")
    val total = column[Int]("TOTAL")
    val id = column[Option[Int], Int]("COF_ID", PrimaryKey, AutoIncrement)

    override def query = q.to[Coffee](coffees)
  }
}
```


You may have noticed that the supplier ID in `coffees` now has a type-safe reference. The second type argument of `column` denotes the underlying SQL type, which in case of foreign keys is an integer.

### Creating table
As previously, create the tables using `create`:

```scala
import MapperDatastore._

withSession { implicit session =>
  create(suppliers, coffees)
}
```

**Output:**
```
3
```



## Entities
Along with the table definition, you have to declare an accompanying `case class`, which is called *entity*. An entity needs to contain exactly the same columns as the table and the columns must have the same types.

A `case class` needs to extend from `Entity`. Furthermore, it needs to define the table that the columns map to.

```scala
case class Supplier(name: String,
                    street: String,
                    city: String,
                    state: Option[String],
                    zip: String,
                    id: Option[Int] = None) extends Entity[Supplier] {
  def columns = mapTo[Supplier](MapperDatastore.suppliers)
}
```


```scala
case class Coffee(name: String,
                  supID: Ref[Supplier],
                  price: Double,
                  sales: Int,
                  total: Int,
                  id: Option[Int] = None) extends Entity[Coffee] {
  def columns = mapTo[Coffee](MapperDatastore.coffees)
}
```


Though all of these fields are in the same order as the table, this is not required to be the case. Mapping takes place based on the field name to the column name in the table, so order doesn’t matter.


## Insert
We’ve create a `Supplier` case class, but now we need to create an instance and insert it into the database:

```scala
import MapperDatastore._

withSession { implicit session =>
  val starbucks = Supplier("Starbucks", "123 Everywhere Rd.", "Lotsaplaces", Some("CA"), "93966")
  starbucks.insert.result
}
```

**Output:**
```
1
```

It is worth noting here that the result is the database-generated primary key.

Now define some global IDs first that we will use throughout this chapter:

```scala
object Ids {
  var acmeId: Ref[Supplier] = _
  var superiorCoffeeId: Ref[Supplier] = _
  var theHighGroundId: Ref[Supplier] = _
}
```


And insert some additional suppliers and capture their ids:

```scala
import MapperDatastore._
import Ids._

transaction { implicit session =>
  acmeId = Supplier("Acme, Inc.", "99 Market Street", "Groundsville", Some("CA"), "95199").insert.result
  superiorCoffeeId = Supplier("Superior Coffee", "1 Party Place", "Mendocino", None, "95460").insert.result
  theHighGroundId = Supplier("The High Ground", "100 Coffee Lane", "Meadows", Some("CA"), "93966").insert.result

  (acmeId, superiorCoffeeId, theHighGroundId)
}
```

**Output:**
```
(2,3,4)
```


## Batch inserting
Now that we have some suppliers, we need to add some coffees as well:

```scala
import MapperDatastore._
import Ids._

withSession { implicit session =>
  Coffee("Colombian", acmeId, 7.99, 0, 0).insert.
    and(Coffee("French Roast", superiorCoffeeId, 8.99, 0, 0).insert).
    and(Coffee("Espresso", theHighGroundId, 9.99, 0, 0).insert).
    and(Coffee("Colombian Decaf", acmeId, 8.99, 0, 0).insert).
    and(Coffee("French Roast Decaf", superiorCoffeeId, 9.99, 0, 0).insert).result
}
```

**Output:**
```
List(1, 2, 3, 4, 5)
```

Note that we need to use type-safe references for the suppliers.


## Query
We’ve successfully inserted our `Supplier` instance. The syntax for querying it back out is similar to SQL:

```scala
import MapperDatastore._
import suppliers._

withSession { implicit session =>
  val query = select (*) from suppliers where name === "Starbucks"
  query.to[Supplier](suppliers).result.head
}
```

**Output:**
```
SUPPLIERS(SUP_NAME: Starbucks, STREET: 123 Everywhere Rd., CITY: Lotsaplaces, STATE: Some(CA), ZIP: 93966, SUP_ID: Some(1))
```

The mapper will automatically match column names in the results to fields in the `case class` provided. Every query can have its own class for convenience mapping.

### Using references
Use `ref` on a table definition to obtain its reference. It can then be used in queries and compared to foreign key columns like `supID`.

```scala
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
```

**Output:**
```
COFFEES(COF_NAME: Colombian, SUP_NAME: Acme, Inc.)
COFFEES(COF_NAME: French Roast, SUP_NAME: Superior Coffee)
COFFEES(COF_NAME: Espresso, SUP_NAME: The High Ground)
COFFEES(COF_NAME: Colombian Decaf, SUP_NAME: Acme, Inc.)
COFFEES(COF_NAME: French Roast Decaf, SUP_NAME: Superior Coffee)
```


### Using joins
Joins are one of the major points where ScalaRelational diverges from other frameworks that have a concept of an ORM:

```scala
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
```

**Output:**
```
Coffee: Coffee(French Roast,3,8.99,0,0,Some(2))
Supplier: Supplier(Superior Coffee,1 Party Place,Mendocino,None,95460,Some(3))
```

This is an efficient SQL query to join the `coffees` table with the `suppliers` table and get back a single result set. Using the mapper we are able to separate the columns relating to `coffees` from `suppliers` and map them directly to our `case class`es.



## Polymorphic tables
It may be desired to represent a type hierarchy in a single table for better performance:

```scala
trait User {
  def name: String
  def id: Option[Int]
}
```


```scala
case class UserGuest(name: String, id: Option[Int] = None) extends User with Entity[UserGuest] {
  def columns = mapTo[UserGuest](UsersDatastore.users)

  val isGuest = true
}
```


```scala
case class UserAdmin(name: String, canDelete: Boolean, id: Option[Int] = None) extends User with Entity[UserAdmin] {
  def columns = mapTo[UserAdmin](UsersDatastore.users)

  val isGuest = false
}
```


```scala
object UsersDatastore extends H2Datastore(mode = H2Memory("mapper")) {
  object users extends MappedTable[User]("users") {
    val id = column[Option[Int], Int]("id", PrimaryKey, AutoIncrement)
    val name = column[String]("name")
    val canDelete = column[Boolean]("canDelete", Polymorphic)
    val isGuest = column[Boolean]("isGuest")

    override def query: Query[Vector[SelectExpression[_]], User] = {
      q.poly[User](qr =>
        if (qr(users.isGuest)) converter[UserGuest](users)
        else                   converter[UserAdmin](users)
      )
    }
  }
}
```


Create the tables:

```scala
import UsersDatastore._

withSession { implicit session =>
  create(users)
}
```

**Output:**
```
1
```

Now you can insert a heterogeneous list of entities:

```scala
import UsersDatastore._

withSession { implicit session =>
  UserGuest("guest").insert.result
  UserAdmin("admin", canDelete = true).insert.result
}
```

**Output:**
```
2
```

To query the table, you will need to evaluate the column which encodes the original type of the object, namely `isGuest` in this case. For more complex type hierarchies you may want to use an enumeration instead of a boolean flag.

```scala
import UsersDatastore._
val query = users.query

withSession { implicit session =>
  query.result.toList.mkString("\n")
}
```

**Output:**
```
users(id: Some(1), name: guest, canDelete: null, isGuest: true)
users(id: Some(2), name: admin, canDelete: true, isGuest: false)
```



