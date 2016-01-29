name := "manual"

version := "1.3.0"

scalaVersion := "2.11.7"

libraryDependencies += "pl.metastack" %% "metadocs" % "0.1.1"

libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "4.1.1.201511131810-r"

libraryDependencies += "org.scalarelational" %% "scalarelational-h2" % "1.3.0"

libraryDependencies += "org.scalarelational" %% "scalarelational-mapper" % "1.3.0"

enablePlugins(BuildInfoPlugin)

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "org.scalarelational"
