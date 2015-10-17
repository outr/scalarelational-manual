#Architecture
This chapter discusses the architecture of ScalaRelational.

##JDBC
All underlying drivers are provided by JDBC. The drivers are blocking.

##Macros
Internally macros are used instead of *reflection* for performance-critical areas.