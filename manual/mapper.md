#Mapper
[package value="org.scalarelational.manual.mapper"]
The mapper module provides functionality to map table rows when persisting or selecting rows.

##sbt dependency
We must first add another dependency to our build file:

```scala
libraryDependencies += "org.scalarelational" %% "scalarelational-mapper" % "%version%"

libraryDependencies += "org.scalarelational" %% "scalarelational-h2" % "%version%"
```

##Library imports
For the mapper you need the following additional import:

[scala type="imports" file="MapperDatastore"]

##Table definition
When defining a table definition with the mapper, the key difference is that you need to use `MappedTable`  and supply the `case class` you want to map it to. We change the example from the previous chapter to:

[scala type="object" file="MapperDatastore"]

You may have noticed that the supplier ID in `coffees` now has a type-safe reference. The second type argument of `column` denotes the underlying SQL type, which in case of foreign keys is an integer.

###Creating table
As previously, create the tables using `create`:

[scala type="section" value="create" file="Mapper"]

##Entities
Along with the table definition, you have to declare an accompanying `case class`, which is called *entity*. An entity needs to contain exactly the same columns as the table and the columns must have the same types.

A `case class` needs to extend from `Entity`. Furthermore, it needs to define the table that the columns map to.

[scala type="case class" file="Supplier"]

[scala type="case class" file="Coffee"]

Though all of these fields are in the same order as the table, this is not required to be the case. Mapping takes place based on the field name to the column name in the table, so order doesn't matter.

##Insert
We've create a `Supplier` case class, but now we need to create an instance and insert it into the database:

[scala type="section" value="insertSupplier" file="Mapper"]

It is worth noting here that the result is the database-generated primary key.

Now define some global IDs first that we will use throughout this chapter:

[scala type="object" file="Ids"]

And insert some additional suppliers and capture their ids:

[scala type="section" value="insertSuppliers" file="Mapper"]

##Batch inserting
Now that we have some suppliers, we need to add some coffees as well:

[scala type="section" value="insertBatch" file="Mapper"]

Note that we need to use type-safe references for the suppliers.

##Query
We've successfully inserted our `Supplier` instance. The syntax for querying it back out is similar to SQL:

[scala type="section" value="queryBasic" file="Mapper"]

The mapper will automatically match column names in the results to fields in the `case class` provided. Every query can have its own class for convenience mapping.

###Using references
Use `ref` on a table definition to obtain its reference. It can then be used in queries and compared to foreign key columns like `supID`.

[scala type="section" value="queryRefs" file="Mapper"]

###Using joins
Joins are one of the major points where ScalaRelational diverges from other frameworks that have a concept of an ORM:

[scala type="section" value="queryJoins" file="Mapper"]

This is an efficient SQL query to join the `coffees` table with the `suppliers` table and get back a single result set. Using the mapper we are able to separate the columns relating to `coffees` from `suppliers` and map them directly to our `case class`es.

##Polymorphic tables
It may be desired to represent a type hierarchy in a single table for better performance:

[scala type="trait" file="User"]

[scala type="case class" file="UserGuest"]

[scala type="case class" file="UserAdmin"]

[scala type="object" file="UsersDatastore"]

Create the tables:

[scala type="section" value="userCreate" file="Mapper"]

Now you can insert a heterogeneous list of entities:

[scala type="section" value="usersInsert" file="Mapper"]

To query the table, you will need to evaluate the column which encodes the original type of the object, namely `isGuest` in this case. For more complex type hierarchies you may want to use an enumeration instead of a boolean flag.

[scala type="section" value="usersQuery" file="Mapper"]
