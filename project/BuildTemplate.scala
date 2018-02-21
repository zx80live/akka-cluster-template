import com.earldouglas.xsbtwebplugin.PluginKeys._
import com.earldouglas.xsbtwebplugin.WebPlugin
import com.earldouglas.xsbtwebplugin.WebPlugin.container
import org.scalatra.sbt.ScalatraPlugin
import sbt.Keys._
import sbt.{Def, _}

import scala.util._

object BuildTemplate extends Build {

  val predefined = Seq(
    Resolver.sbtPluginRepo("releases"),
    Resolver.sbtPluginRepo("snapshots"),
    Resolver.typesafeIvyRepo("releases"),
    Resolver.typesafeIvyRepo("snapshots"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  )

  val repositories = Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe Maven" at "http://repo.typesafe.com/typesafe/maven-releases/",
    "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
    "Maven Central Repository" at "http://search.maven.org",
    "Scala Tools" at "http://scala-tools.org/repo-snapshots/",
    "sbt-idea-repo" at "http://mpeltonen.github.com/maven/",
    "Akka" at "http://repo.akka.io/releases/",
    "confluent" at "http://packages.confluent.io/maven/"
  )

  val akkaVersion = "2.5.9"

  val dependencies = Seq(
    "org.scala-lang" % "scala-reflect" % "2.11.7",
    "org.reflections" % "reflections" % "0.9.10",
    "org.scalatra" % "scalatra_2.11" % "2.3.1",
    "org.scalatra" %% "scalatra-json" % "2.4.+",
    "org.scalatra" %% "scalatra-scalatest" % "2.4.+" % "test",
    "org.scalatra" %% "scalatra-auth" % "2.4.1",
    "org.eclipse.jetty" % "jetty-webapp" % "9.1.+" % "compile, container",
    "org.eclipse.jetty" % "jetty-plus" % "9.1.+" % "compile, container",
    "javax.servlet" % "javax.servlet-api" % "3.0.+",
    "com.typesafe" % "config" % "1.0.+",
    "com.amazonaws" % "aws-java-sdk" % "1.10.2" excludeAll(
      ExclusionRule(organization = "com.amazonaws", name = "aws-java-sdk-importexport"),
      ExclusionRule(organization = "com.amazonaws", name = "aws-java-sdk-codedeploy"),
      ExclusionRule(organization = "com.amazonaws", name = "aws-java-sdk-efs")
    ),
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,

    "org.scalatra" %% "scalatra-specs2" % "2.3.0" % "test",
    "org.scalatra" %% "scalatra-swagger" % "2.4.+",
    "ch.qos.logback" % "logback-classic" % "1.1.8",
    "org.scalatra" % "scalatra-scalate_2.11" % "2.4.0-RC2-2",
    "net.kencochrane.raven" % "raven-logback" % "6.0.0"
  )


  val buildId = Properties.envOrElse(
    "BUILD_NUMBER",
    "SNAPSHOT"
  )

  val majorVersion = "0.0"

  val sharedDefinitions = Seq(
    organization := "unknown",
    scalaVersion := "2.11.12",
    scalacOptions ++= Nil,
    publishMavenStyle := true,
    crossPaths := false,
    version := majorVersion + "." + buildId,
    parallelExecution in Test := false,

    dependencyOverrides := Set(
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    )
  )

  var buildSettings: Seq[Def.Setting[_]] = Defaults.defaultSettings ++ Seq()
  buildSettings ++= Seq(libraryDependencies ++= dependencies.map(_.exclude("org.slf4j", "slf4j-jdk14")))
  buildSettings ++= ScalatraPlugin.scalatraSettings
  buildSettings ++= Seq(resolvers ++= repositories)
  buildSettings ++= Seq(resolvers ++= predefined)
  buildSettings ++= Seq(scalacOptions += "")
  buildSettings ++= sharedDefinitions
  buildSettings ++= Seq(javaOptions += "-javaagent:" + System.getProperty("user.home") + "/.ivy2/cache/org.aspectj/aspectjweaver/jars/aspectjweaver-1.7.2.jar", fork := true)

  val helper = (p: Project) => p.settings(buildSettings: _*)

  val core = helper(project in file("core"))

  val master = helper(project.settings(WebPlugin.webSettings)).dependsOn(core)
    .settings(port in container.Configuration := 8081)

  val worker = helper(project.settings(WebPlugin.webSettings))
    .dependsOn(core)
    .settings(port in container.Configuration := 8082)

  var root: Project = Project(
    id = "akka-cluster-template",
    settings = buildSettings,
    base = file(".")) aggregate(
    master,
    worker
  )
}
