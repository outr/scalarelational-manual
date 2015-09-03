import java.io.File

import org.joda.time.DateTime

import pl.metastack.metadocs.document._
import pl.metastack.metadocs.document.writer._
import pl.metastack.metadocs.document.writer.html.Components
import pl.metastack.metadocs.input._

object Main {
  def main(args: Array[String]) {
    val meta = Meta(
      date = DateTime.now(),
      title = "ScalaRelational User Manual v1.1.0",
      author = "Matt Hicks, Tim Nieradzik",
      affiliation = "OUTR Technologies, LLC",
      `abstract` = "ScalaRelational is a type-safe framework for defining, modifying, and querying SQL databases in Scala.",
      language = "en-GB",
      url = ""
    )

    val files = new File("chapters")
      .listFiles()
      .map(_.getPath)
      .filter(_.endsWith(".txt"))
      .sorted

    val instructionSet = DefaultInstructionSet
      .inherit(BookInstructionSet)
      .inherit(CodeInstructionSet)
      .inherit(DraftInstructionSet)
      .withAliases(
        "b" -> Bold,
        "i" -> Italic,
        "item" -> ListItem
      )

    val rawTree = Document.loadFiles(files)
    val docTree = Document.toDocumentTree(
      rawTree,
      instructionSet,
      generateId = caption => Some(caption.collect {
        case c if c.isLetterOrDigit => c
        case c if c.isSpaceChar => '-'
      }.toLowerCase)
    )

    // Explicitly print out all chapters/sections which is useful when
    // restructuring the document
    println("Document tree:")
    println(Extractors.references(docTree))
    println()

    Document.printTodos(docTree)

    val sbtScala = new SbtScala("projects")
    val docTreeWithExternalCode = sbtScala.createProjects(docTree)
    sbtScala.runProjects(docTree)

    val docTreeWithOutput = sbtScala.embedOutput(docTreeWithExternalCode)

    val skeleton = Components.pageSkeleton(
      cssPaths = Seq(
        "css/kult.css",
        "css/highlight.css"
      ),
      jsPaths = Seq(
        "//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js",
        "js/main.js",
        "js/highlight.js"
      ),
      script = Some("hljs.initHighlightingOnLoad();"),
      favicon = Some("images/favicon.ico")
    )(_, _, _)

    html.document.SinglePage.write(docTreeWithOutput,
      skeleton,
      "manual/single-page.html",
      meta = Some(meta),
      toc = true,
      tocDepth = 2)  // Don't include subsections

    html.document.Book.write(docTreeWithOutput,
      skeleton,
      "manual",
      meta = Some(meta),
      tocDepth = 2)
  }
}
