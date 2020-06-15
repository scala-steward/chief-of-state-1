package com.namely.chiefofstate

import akka.actor.ActorSystem
import com.google.protobuf.any.Any
import com.namely.protobuf.chief_of_state.handler.{HandleEventRequest, HandleEventResponse, HandlerServiceClient}
import com.namely.protobuf.chief_of_state.persistence.{Event, State}
import lagompb.{LagompbEventHandler, LagompbException}
import lagompb.core.MetaData
import org.slf4j.{Logger, LoggerFactory}
import scalapb.GeneratedMessage

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * ChiefOfStateEventHandler
 *
 * @param actorSystem    the actor system
 * @param gRpcClient     the gRpcClient used to connect to the actual event handler
 * @param handlerSetting the event handler setting
 */
class ChiefOfStateEventHandler(
    actorSystem: ActorSystem,
    gRpcClient: HandlerServiceClient,
    handlerSetting: ChiefOfStateHandlerSetting
) extends LagompbEventHandler[State](actorSystem) {

  final val log: Logger = LoggerFactory.getLogger(getClass)

  override def handle(event: GeneratedMessage, priorState: State, eventMeta: MetaData): State = {
    Try(
      gRpcClient.handleEvent(
        HandleEventRequest()
          .withEvent(event.asInstanceOf[Event].getEvent)
          .withCurrentState(priorState.getCurrentState)
          .withMeta(Any.pack(eventMeta))
      )
    ) match {

      case Failure(e) =>
        throw new LagompbException(e.getMessage)

      case Success(eventualEventResponse: Future[HandleEventResponse]) =>
        Try {
          Await.result(eventualEventResponse, Duration.Inf)
        } match {
          case Failure(exception) => throw new LagompbException(exception.getMessage)
          case Success(handleEventResponse: HandleEventResponse) =>
            val stateFQN: String = ChiefOfStateHelper.getProtoFullyQualifiedName(handleEventResponse.getResultingState)

            log.debug(s"[ChiefOfState]: event handler state $stateFQN")

            if (handlerSetting.stateProtoFQN.equals(stateFQN)) {

              log.debug(s"[ChiefOfState]: event handler state $stateFQN is valid.")

              priorState.update(_.currentState := handleEventResponse.getResultingState)
            } else {
              throw new LagompbException(
                s"[ChiefOfState]: command handler state to perist $stateFQN is not configured. Failing request"
              )
            }
        }
    }
  }
}
