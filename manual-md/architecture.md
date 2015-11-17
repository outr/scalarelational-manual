Previous chapter: [Table definition](table-definition-1.md)  |  Next chapter: [Databases](databases.md)  |  [Edit source](https://github.com/outr/scalarelational-manual/edit/master/manual/architecture.md)

# Architecture
This chapter discusses the architecture of ScalaRelational.

## JDBC
All underlying drivers are provided by JDBC. The drivers are blocking.


## Macros
Internally macros are used instead of *reflection* for performance-critical areas.



