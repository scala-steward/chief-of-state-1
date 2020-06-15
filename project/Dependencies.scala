package com.namely.chiefofstate

import sbt._

object Dependencies {

  // Package versions
  object Versions {
    val scala213 = "2.13.1"
    val lagompVersion = "0.2.0"
    val akkaVersion: String = "2.6.6"
    val scalapbCommonProtosVersion: String = "1.18.0-0"
    val kanelaAgentVersion = "1.0.5"
    val silencerVersion = "1.6.0"
    val kamonAkkaGrpcVersion = "0.0.9"
  }

  object Compile {
    val lagompb: ModuleID = "io.superflat" %% "lagompb-core" % Versions.lagompVersion
    val lagompbReadSide = "io.superflat" %% "lagompb-readside" % Versions.lagompVersion

    val scalapbCommon
      : ModuleID = "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.10" % Versions.scalapbCommonProtosVersion
    val kanelaAgent = "io.kamon" % "kanela-agent" % Versions.kanelaAgentVersion
    val kamonAkkaGrpc = "com.github.nezasa" %% "kamon-akka-grpc" % Versions.kamonAkkaGrpcVersion intransitive ()
  }

  object Runtime {
    val lagompbRuntime: ModuleID = "io.superflat" %% "lagompb-core" % Versions.lagompVersion % "protobuf"

    val scalapbCommonProtos
      : ModuleID = "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.10" % Versions.scalapbCommonProtosVersion % "protobuf"
    val grpcNetty = "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion
    val scalapbGrpcRuntime = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    val scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  }

  object Test {
    final val Test = sbt.Test
    val akkaGrpcTestkit = "com.lightbend.play" %% "lagom-scaladsl-grpc-testkit" % "0.8.2"
  }

}
