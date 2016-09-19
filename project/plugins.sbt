logLevel := Level.Warn

addSbtPlugin("eu.akkamo" % "sbt-akkamo" % "1.0.0")

resolvers ++= Seq(
  Resolver.bintrayRepo("websudos", "oss-releases"),
  Resolver.url("Websudos OSS", url("http://dl.bintray.com/websudos/oss-releases"))(Resolver.ivyStylePatterns)
)

// And finally the plugin dependency itself
addSbtPlugin("com.websudos" %% "phantom-sbt" % "1.22.0")
