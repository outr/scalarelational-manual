import java.io.File

import org.joda.time.DateTime

import pl.metastack.metadocs.input._
import pl.metastack.metadocs.document._
import pl.metastack.metadocs.document.writer._
import pl.metastack.metadocs.document.writer.html.Components

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

    val chapters = Seq(
      "introduction",
      "getting_started",
      "mapper",
      "querying",
      "tabledef",
      "architecture",
      "databases",
      "support",
      "contributing",
      "changelog",
      "benchmarks",
      "license"
    )

    val files = chapters.map(chapter =>
      new File(s"src/main/resources/$chapter.md"))

    val instructionSet = DefaultInstructionSet
      .inherit(BookInstructionSet)
      .inherit(CodeInstructionSet)
      .inherit(DraftInstructionSet)
      .withAliases(
        "b" -> Bold,
        "i" -> Italic,
        "item" -> ListItem
      )

    val rawTrees = files.flatMap(file =>
      Markdown.loadFileWithExtensions(file,
        instructionSet,
        generateId = caption => Some(caption.collect {
          case c if c.isLetterOrDigit => c
          case c if c.isSpaceChar => '-'
        }.toLowerCase)
      ).toOption
    )

    val docTree = Document.mergeTrees(rawTrees)

    // Explicitly print out all chapters/sections which is useful when
    // restructuring the document
    println("Document tree:")
    println(Extractors.references(docTree))
    println()

    Document.printTodos(docTree)

    val pipeline =
      Document.pipeline
        .andThen(CodeProcessor.embedListings _)
        .andThen(CodeProcessor.embedOutput _)
    val docTreeWithCode = pipeline(docTree)

    val skeleton = Components.pageSkeleton(
      cssPaths = Seq(
        "css/kult.css",
        "css/default.min.css"
      ),
      jsPaths = Seq(
        "//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js",
        "js/main.js",
        "js/highlight.pack.js"
      ),
      script = Some("hljs.initHighlightingOnLoad();"),
      favicon = Some("images/favicon.ico")
    )(_, _, _)

    html.document.SinglePage.write(docTreeWithCode,
      skeleton,
      "manual/single-page.html",
      meta = Some(meta),
      toc = true,
      tocDepth = 2)  // Don't include subsections

    html.document.Book.write(docTreeWithCode,
      skeleton,
      "manual",
      meta = Some(meta),
      tocDepth = 2)
  }
}
