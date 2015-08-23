import java.io.File

import pl.metastack.metadocs.document.{Meta, Document}
import pl.metastack.metadocs.document.writer.{SbtScala, HTMLDocument}
import pl.metastack.metadocs.input._

object Main {
  def main(args: Array[String]) {
    val meta = Meta(
      date = "August 2015",
      title = "ScalaRelational User Manual v1.1.0",
      author = "Matt Hicks, Tim Nieradzik",
      affiliation = "OUTR Technologies, LLC",
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
      generateId = caption => Some(caption.map {
        case c if c.isSpaceChar => '-'
        case c => c
      }.toLowerCase)
    )

    // Don't include subsections
    val toc = Document.generateTOC(docTree, maxLevel = 2)

    // Explicitly print out the TOC which is useful when restructuring the document
    println("Table of contents:")
    println(toc)
    println()

    Document.printTodos(docTree)

    val sbtScala = new SbtScala("projects")
    sbtScala.createProjects(docTree)
    sbtScala.runProjects(docTree)

    val docTreeWithOutput = sbtScala.embedOutput(docTree)

    HTMLDocument.write(docTreeWithOutput,
      "manual.html",
      cssPath = Some("styles/kult.css"),
      meta = Some(meta),
      toc = Some(toc))
  }
}
