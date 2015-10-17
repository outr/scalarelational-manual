#Querying
[package value="org.scalarelational.manual.querying"]
##Session management
All database queries must take place within a *session*. The session manages the database connection on your behalf.

Sessions within sessions are valid. ScalaRelational will ignore inner-session creation and only maintain a connection for the outermost session. Additionally, sessions are lazy and will only open a connection when one is needed, so it is perfectly acceptable to wrap blocks of code that may or may not access the database without being concerned about the performance implications.

##Transactions
Sessions should not be confused with *transactions*. A session simply manages the connection whereas a transaction disables [autocommit](https://en.wikipedia.org/wiki/Autocommit) and automatically rolls back if an error occurs while executing.

##Select
We will re-use the database from the previous chapter.

We've finished inserting data now, so lets start querying some data back out. We'll start by simply iterating over the coffees in the database and printing them out:

[scala type="section" value="basicCoffees" file="Querying"]

`toList` is necessary because `result` returns a `QueryResultsIterator` which streams from the database and expects an active session. If you plan to use the rows outside of the session you must convert them to a concrete sequence first.

It is worthwhile to notice that our query looks exactly like a SQL query, but it is Scala code using ScalaRelational's DSL to provide this. Most of the time SQL queries in ScalaRelational look exactly the same as in SQL, but there are a few more complex scenarios where this is not the case or even not preferred.

###Joining
Now that we've seen a basic query example, let's look at a more advanced scenario. We will query all coffees filtering and joining with suppliers:

[scala type="section" value="joinQuery" file="Querying"]

[scala type="section" value="join" file="Querying"]

You can see here that though this query looks very similar to an SQL query there are some slight differences. This is the result of Scala's limitations for writing DSLs. For example, we must use three equal signs (`===`) instead of two.

ScalaRelational tries to retain type information where possible, and though loading data by columns is clean, we can actually extract a `(String, String)` tuple representing the coffee name and supplier's name:

[scala type="section" value="tuple" file="Querying"]

This is possible because the DSL supports explicit argument lists and retains type information all the way through to the result.

##Update
This is an example for updating a row:

[scala type="section" value="update" file="Querying"]

##Delete
In analogy to updating rows, a deletion looks as follows:

[scala type="section" value="delete" file="Querying"]

##SQL functions
ScalaRelational supports SQL functions such as `min` and `max`. These are defined directly on the columns like the following:

[scala type="section" value="functions" file="Querying"]

