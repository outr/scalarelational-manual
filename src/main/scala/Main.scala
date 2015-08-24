import java.io.File

import pl.metastack.metadocs.document._
import pl.metastack.metadocs.document.writer._
import pl.metastack.metadocs.input._

object Main {
  def main(args: Array[String]) {
    val meta = Meta(
      date = "August 2015",
      title = "ScalaRelational User Manual v1.1.0",
      author = "Matt Hicks, Tim Nieradzik",
      affiliation = "OUTR Technologies, LLC",
      `abstract` = "ScalaRelational is a type-safe framework for defining, modifying, and querying SQL databases in Scala.",
      language = "en-GB"
    )

    val files = new File("chapters")
      .listFiles()
      .map(_.getPath)
      .filter(_.endsWith(".txt"))
      .sorted

    val instructionSet = DefaultInstructionSet
      .inherit(CodeInstructionSet)
      .inherit(DraftInstructionSet)
      .withAliases(Map(
        "b" -> Bold,
        "i" -> Italic,
        "item" -> ListItem
      ))

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
    sbtScala.createProjects(docTree)
    sbtScala.runProjects(docTree)

    val docTreeWithOutput = sbtScala.embedOutput(docTree)

    html.document.SinglePage.write(docTreeWithOutput,
      "manual.html",
      cssPath = Some("styles/kult.css"),
      meta = Some(meta),
      toc = true,
      tocDepth = 2)  // Don't include subsections

    html.document.MultiPage.write(docTreeWithOutput,
      "manual",
      cssPath = Some("../styles/kult.css"),
      meta = Some(meta),
      tocDepth = 2)
  }
}
