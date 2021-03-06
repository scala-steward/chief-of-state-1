test / parallelExecution := false
Test / fork := true

lazy val root: Project = project
  .in(file("."))
  .enablePlugins(NoPublish)
  .enablePlugins(UniversalPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    headerLicense := None,
    Compile / mainClass := Some("com.github.chiefofstate.StartNode"),
    makeBatScripts := Seq(),
    executableScriptName := "entrypoint",
    Universal / javaOptions ++= Seq(
      // -J params will be added as jvm parameters
      "-J-Xms256M",
      "-J-Xmx1G",
      "-J-XX:+UseG1GC"))
  .dependsOn(chiefofstate)
  .aggregate(protogen, chiefofstate, protogenTest, migration)

lazy val chiefofstate: Project = project
  .in(file("code/service"))
  .enablePlugins(Common)
  .enablePlugins(BuildSettings)
  .enablePlugins(NoPublish)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(name := "chiefofstate", headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax)
  .dependsOn(protogen, protogenTest % "test->compile", migration)

lazy val migration = project
  .in(file("code/migration"))
  .enablePlugins(Common)
  .enablePlugins(BuildSettings)
  .enablePlugins(NoPublish)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    name := "migration",
    description := "data migration tool",
    headerLicenseStyle := HeaderLicenseStyle.SpdxSyntax)
  .dependsOn(protogen)

lazy val protogen: Project = project
  .in(file("code/.protogen"))
  .enablePlugins(Common)
  .enablePlugins(BuildSettings)
  .enablePlugins(NoPublish)
  .settings(name := "protogen")
  .settings(headerLicense := None)
  .settings(inConfig(Compile)(Seq(
    PB.protoSources := Seq(
      // instruct scalapb to build all COS protos
      file("proto/chief-of-state-protos/chief_of_state"),
      file("proto/internal")),
    PB.includePaths := Seq(
      // includes the protobuf source for imports
      file("proto/chief-of-state-protos"),
      file("proto/internal"),
      // includes external protobufs (like google dependencies)
      baseDirectory.value / "target/protobuf_external"),
    PB.targets := Seq(scalapb
      .gen(flatPackage = false, javaConversions = false, grpc = true) -> (Compile / sourceManaged).value / "scalapb"))))

lazy val protogenTest: Project = project
  .in(file("code/.protogen_test"))
  .enablePlugins(Common)
  .enablePlugins(BuildSettings)
  .enablePlugins(NoPublish)
  .settings(name := "protogen_test")
  .settings(headerLicense := None)
  .settings(inConfig(Compile)(Seq(
    PB.protoSources := Seq(file("proto/test")),
    PB.includePaths := Seq(
      file("proto/test"),
      // includes external protobufs (like google dependencies)
      baseDirectory.value / "target/protobuf_external"),
    PB.targets := Seq(scalapb
      .gen(flatPackage = false, javaConversions = false, grpc = true) -> (Compile / sourceManaged).value / "scalapb"))))
