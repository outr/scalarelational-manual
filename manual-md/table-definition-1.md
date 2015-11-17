Previous chapter: [Querying](querying-1.md)  |  Next chapter: [Architecture](architecture.md)  |  [Edit source](https://github.com/outr/scalarelational-manual/edit/master/https://github.com/outr/scalarelational-manual/edit/master/)

# Table definition


## Mapper
See the chapter [mapper](mapper.md) for more information.


## Column types

## References
References are a type-safe abstraction for foreign keys:

```scala
val supID = column[Ref[Supplier], Int]("SUP_ID", new ForeignKey(suppliers.id))
```
You can call `ref` on every table to obtain its reference.

### Option
We prevent `null` pointers by introducing an optional column type:

```scala
val id = column[Option[Int], Int]("COF_ID", PrimaryKey, AutoIncrement)
```
The second type parameter denotes the underlying SQL type.

You can call `opt` on every column to obtain its values wrapped in an optional column type. This is sometimes necessary when comparing an optional column with a non-optional column in a query:

```scala
import MapperDatastore._

val query = (
  select (coffees.*)
    from coffees
    innerJoin suppliers
    on suppliers.ref === coffees.supID
)
```



### Blobs
A column serialising an object looks as follows:

```scala
val fruit = column[Fruit, Blob]("fruit", ObjectSerializationDataTypeCreator.create[Fruit])
```
There is also an untyped `Blob` column type:

```scala
val content = column[Blob]("content")
```


## Defining a custom type
Example for storing string lists in a `VARCHAR` column:

```scala
object ListConverter extends SQLConversion[List[String], String] {
  override def toSQL(column: ColumnLike[List[String], String], value: List[String]): String = value.mkString("|")
  override def fromSQL(column: ColumnLike[List[String], String], value: String): List[String] = value.split('|').toList
}
implicit def listDataType = new DataType[List[String], String](Types.VARCHAR, SQLType("VARCHAR(1024)"), ListConverter)
```

## Column properties
### Unique values
Example:

```scala
val name = column[String]("name", Unique)
```

### Custom column length
The pre-defined column types use heuristic values for the default column lengths. It may happen that the default value is not a good fit. You can change this by passing the column property `ColumnLength`:

```scala
val name = column[String]("name", ColumnLength(200))
```


## Versioning
Add `with VersioningSupport` to your datastore definition and define a new upgrade instance for every new version:

```scala
object Upgrade4 extends UpgradableVersion {
  override def version = 4
  override def upgrade() = {
    import VersioningDatastore._

    createTable("test").
      and(createColumn[Int]("test2", "id", PrimaryKey, AutoIncrement)).
      and(createColumn[String]("test2", "name")).
      and(createColumn[Option[Int], Int]("test2", "age")).result
  }
}
```
Within the datastore, call `register` for all upgrade objects:

```scala
register(Upgrade3)
register(Upgrade4)
```
To obtain the current version, use `version()`. To upgrade to the latest version, call `upgrade()` in a session.



