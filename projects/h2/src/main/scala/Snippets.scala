import org.scalarelational.column.property._
import org.scalarelational.h2.{H2Datastore, H2Memory}
import org.scalarelational.table.Table

object Snippets {
  def main() {
    // {{{ join
      import ExampleDatastore._
      import coffees._

      session {
        val query = (select(coffees.name, suppliers.name)
          from coffees
          innerJoin suppliers on coffees.supID === suppliers.id
          where coffees.price < 9.0)

        query.result.map { r =>
          s"Coffee: ${r(coffees.name)}, Supplier: ${r(suppliers.name)}"
        }.mkString("\n")
      }
    // }}}
  }
}
