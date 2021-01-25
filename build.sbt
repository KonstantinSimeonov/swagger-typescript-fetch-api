name := "swagger-ts-fetch-api"

version := "1.0.0"

scalaVersion := "2.13.1"

val SwaggerCodegenVersion = "2.2.2"

libraryDependencies ++= Seq(
  "io.swagger" % "swagger-codegen" % SwaggerCodegenVersion
)
