/*
 * Copyright 2020 chiefofstate
 *
 * SPDX-License-Identifier: MIT
 */

package com.github.chiefofstate

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.github.chiefofstate.migration.{ JdbcConfig, Migrator }
import com.github.chiefofstate.migration.versions.v6.V6
import com.github.chiefofstate.protobuf.v1.internal.{ DoMigration, MigrationDone }
import com.github.chiefofstate.serialization.{ MessageWithActorRef, ScalaMessage }
import com.typesafe.config.Config
import org.slf4j.{ Logger, LoggerFactory }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.{ Failure, Success, Try }

/**
 * kick starts the various migrations needed to run.
 * When the migration process is successful it replies back to the [[ServiceBootstrapper]] to continue the boot process.
 * However when the migration process fails it stops and the [[StartNodeBehaviour]] get notified and halt the whole
 * boot process. This is the logic behind running the actual migration
 * <p>
 *   <ol>
 *     <li> check the existence of the cos_migrations table
 *     <li> if the table exists go to step 4
 *     <li> if the table does not exist run the whole migration and reply back to the [[ServiceBootstrapper]]
 *     <li> check the current version against the available versions
 *     <li> if current version is the last version then no need to run the migration just reply back to [[ServiceBootstrapper]]
 *     <li> if not then run the whole migration and reply back to the [[ServiceBootstrapper]]
 *   </ol>
 * </p>
 */
object ServiceMigrationRunner {
  final val log: Logger = LoggerFactory.getLogger(getClass)

  def apply(config: Config): Behavior[ScalaMessage] = Behaviors.setup[ScalaMessage] { context =>
    Behaviors.receiveMessage[ScalaMessage] {
      case MessageWithActorRef(message, replyTo) if message.isInstanceOf[DoMigration] =>
        val result: Try[Unit] = {
          // create and run the migrator
          val journalJdbcConfig: DatabaseConfig[JdbcProfile] =
            JdbcConfig.journalConfig(config)
          val schema: String = config.getString("jdbc-default.schema")
          val v6: V6 = V6(journalJdbcConfig)
          // instance of the migrator
          val migrator: Migrator =
            new Migrator(journalJdbcConfig, schema).addVersion(v6)
          migrator.run()
        }

        result match {
          case Failure(exception) =>
            log.error("migration failed", exception)

            // stopping the actor
            Behaviors.stopped

          case Success(_) =>
            log.info("ChiefOfState migration successfully done...")

            replyTo ! MigrationDone.defaultInstance

            Behaviors.same
        }

      case MessageWithActorRef(message, _) =>
        log.warn(s"unhandled message ${message.companion.scalaDescriptor.fullName}")
        Behaviors.stopped

      case unhandled =>
        log.warn(s"cannot serialize ${unhandled.getClass.getName}")

        Behaviors.stopped
    }
  }
}
