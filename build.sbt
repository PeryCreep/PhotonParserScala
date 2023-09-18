ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = Project("PhotonParser", file("."))
  .settings(
    name := "PhotonParser"
  )
