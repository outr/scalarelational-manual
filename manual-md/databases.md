Previous chapter: [Architecture](architecture.md)  |  Next chapter: [Support](support.md)  |  [Edit source](https://github.com/outr/scalarelational-manual/edit/master/manual/databases.md)

# Databases
This chapter discusses ScalaRelational’s database support.

## Compatibility
The following versions have been tested:

| Database  | Version  | sbt dependency  |
| H2  | v1.4.187  | scalarelational-h2  |
| MariaDb/MySQL  | v5.1.36  | scalarelational-mariadb  |
| PostgreSQL  | v9.4-1201-jdbc41  | scalarelational-postgresql  |
## H2
For better performance, [HikariCP](http://brettwooldridge.github.io/HikariCP/) is supported. Add the `HikariSupport` *trait* to your `H2Datastore`.


## MariaDB/MySQL
This library is guaranteed to work with MariaDB. MySQL is supported for the most part, but without any guarantee as new versions are released.

MariaDB and MySQL have a row size limit. If you don’t set a `ColumnLength` for `VARCHAR` (i.e. `String`) explicitly, `ColumnLength(200)` will be used. You can globally override the default length in the `Datastore` as follows:

```scala
object GettingStartedDatastore extends MariaDBDatastore(config) {
  override def DefaultVarCharLength = 200

  object suppliers extends MappedTable[Supplier]("SUPPLIERS") {
    val name = column[String]("SUP_NAME", Unique)
    ...
  }
}
```
Alternatively, you can specify a custom length for some selected columns.

Please note that unlike MariaDB, MySQL does not support the clause `CREATE INDEX IF NOT EXISTS`.



