resolvers += Classpaths.sbtPluginReleases
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Snapshot" at "https://repo.typesafe.com/typesafe/snapshots/"
resolvers += Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.scalatra.sbt" % "scalatra-sbt" % "0.3.5")
addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.+")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")
