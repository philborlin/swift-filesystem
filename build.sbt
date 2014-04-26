organization := "com.risertech"

name := "swift-filesystem"

version := "0.1.0"

// javacOptions ++= Seq("-source", "1.7")

javacOptions ++= Seq("-encoding", "utf-8", "-source", "1.7")

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

unmanagedSourceDirectories in Compile <<= (javaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (javaSource in Test)(Seq(_))

pomIncludeRepository := { _ => false }

autoScalaLibrary := false

crossPaths := false

scalacOptions += "-target:jvm-1.7"

libraryDependencies ++= Seq(
  "org.javaswift" % "joss" % "0.9.4",
  "com.novocode" % "junit-interface" % "0.10" % "test"
)     
