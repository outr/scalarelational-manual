package org.scalarelational

import java.io.File
import java.nio.file.{LinkOption, Files, Paths}

import org.joda.time.DateTime

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import pl.metastack.metadocs.document._
import pl.metastack.metadocs.input._
import pl.metastack.metadocs.input.metadocs._
import pl.metastack.metadocs.output
import pl.metastack.metadocs.output.html.Components
import pl.metastack.metadocs.output.html.document.{Book, SinglePage}

object Main extends App {
  val organisation = "outr"
  val repoName = s"$organisation.github.io"
  val projectName = "scalarelational"
  val projectPath = new File("..", repoName)
  val manualPath = new File(projectPath, projectName)
  val manualPathStr = manualPath.getPath
  val manualVersionPath = new File(manualPath, "v" + BuildInfo.version)
  val manualVersionPathStr = manualVersionPath.getPath
  val imagesPath = new File(manualVersionPath, "images")
  val isSnapshot = BuildInfo.version.endsWith("SNAPSHOT")

  if (!projectPath.exists())
    Git.cloneRepository()
      .setURI(s"git@github.com:$organisation/$repoName.git")
      .setDirectory(projectPath)
      .call()

  manualPath.mkdir()
  manualVersionPath.mkdirs()
  manualVersionPath.listFiles().foreach(_.delete())

  imagesPath.mkdirs()
  imagesPath.listFiles().foreach(_.delete())

  val meta = Meta(
    date = DateTime.now(),
    title = s"ScalaRelational User Manual v${BuildInfo.version}",
    author = "Matt Hicks, Tim Nieradzik",
    affiliation = "OUTR Technologies, LLC",
    `abstract` = "ScalaRelational is a type-safe framework for defining, modifying, and querying SQL databases in Scala.",
    language = "en-GB",
    url = "",
    editSourceURL = Some("https://github.com/outr/scalarelational-manual/edit/master/"))

  val instructionSet = DefaultInstructionSet
    .inherit(BookInstructionSet)
    .inherit(CodeInstructionSet)
    .inherit(DraftInstructionSet)
    .withAliases(
      "b" -> Bold,
      "i" -> Italic,
      "item" -> ListItem)

  val chapters = Seq("introduction", "getting_started", "mapper", "querying",
    "tabledef", "architecture", "databases", "support", "contributing", "changelog",
    "benchmarks", "license")

  val files = chapters.map(chapter => s"manual/$chapter.md")

  val rawTrees = files.flatMap(file =>
    Markdown.loadFileWithExtensions(file,
      instructionSet,
      constants = Map("version" -> BuildInfo.version),
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
      .andThen(CodeProcessor.embedListings(".") _)
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

  SinglePage.write(docTreeWithCode,
    skeleton,
    s"$manualPathStr/v${BuildInfo.version}.html",
    meta = Some(meta),
    toc = true,
    tocDepth = 2)  // Don't include subsections

  Book.write(docTreeWithCode,
    skeleton,
    s"$manualPathStr/v${BuildInfo.version}",
    meta = Some(meta),
    tocDepth = 2)

  output.Markdown.write(docTreeWithCode,
    "manual-md",
    meta = Some(meta),
    tocDepth = 2)

  val links =
    Set("css", "js", "favicon.ico").flatMap { folder =>
      Seq(
        s"$manualPathStr/$folder" -> s"../$folder",
        s"$manualVersionPathStr/$folder" -> s"../../$folder")
    }.toMap ++
      Map(s"$manualPathStr/images" -> s"v${BuildInfo.version}/images") ++ (
      if (isSnapshot) Map.empty
      else Map(
        s"$manualPathStr/latest" -> s"v${BuildInfo.version}",
        s"$manualPathStr/latest.html" -> s"v${BuildInfo.version}.html"))

  links.map { case (from, to) =>
    Paths.get(from) -> Paths.get(to)
  }.foreach { case (from, to) =>
    if (Files.exists(from, LinkOption.NOFOLLOW_LINKS)) Files.delete(from)
    Files.createSymbolicLink(from, to)
  }

  /* Replace images */
  new File("manual", "images")
    .listFiles()
    .filter(_.getName.endsWith(".png"))
    .foreach { image =>
      Files.copy(image.toPath, new File(imagesPath, image.getName).toPath)
    }

  val repo = FileRepositoryBuilder.create(new File(projectPath, ".git"))
  val git = new Git(repo)
  git.add().addFilepattern(projectName).call()
  if (!git.status().call().isClean) {
    git.commit()
      .setAll(true)
      .setMessage(s"Update $projectName v${BuildInfo.version}").call()
    git.push().call()
  }
}
