val ScalatraVersion = "3.0.0"

ThisBuild / scalaVersion := "3.3.1"
ThisBuild / organization := "io.github.vyxal"

lazy val hello = (project in file("."))
  .settings(
    name := "Tyxal",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor" % "1.0.0" cross(CrossVersion.for3Use2_13),
      "com.softwaremill.sttp.client3" %% "core" % "3.9.0",
      "org.json4s"        %% "json4s-native"              % "4.0.6",
      "org.scalatra"      %% "scalatra-swagger-jakarta"   % ScalatraVersion,
      "org.scalatra"      %% "scalatra-jakarta"           % ScalatraVersion,
      "org.scalatra"      %% "scalatra-scalatest-jakarta" % ScalatraVersion % "test",
      "ch.qos.logback"    %  "logback-classic"            % "1.4.11"        % "runtime",
      "org.eclipse.jetty" % "jetty-webapp"                % "11.0.17"       % "container",
      "jakarta.servlet"   % "jakarta.servlet-api"         % "5.0.0"         % "provided"
    ),
  )

enablePlugins(JettyPlugin)

Jetty / containerLibs := Seq("org.eclipse.jetty" % "jetty-runner" % "11.0.17" intransitive())
