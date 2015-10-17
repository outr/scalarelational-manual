package org.scalarelational.manual.tabledef

import pl.metastack.metadocs.SectionSupport
import org.scalarelational.manual.mapper._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TableDef extends SectionSupport {
  section("refs") {
    import MapperDatastore._

    val query = (
      select (coffees.*)
        from coffees
        innerJoin suppliers
        on suppliers.ref === coffees.supID
    )
  }
}