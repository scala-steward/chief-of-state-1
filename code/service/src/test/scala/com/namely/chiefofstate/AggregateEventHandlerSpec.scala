package com.namely.chiefofstate

import java.util.UUID

import akka.grpc.GrpcServiceException
import com.google.protobuf.any.Any
import com.namely.chiefofstate.config.HandlerSetting
import com.namely.chiefofstate.test.helpers.TestSpec
import com.namely.protobuf.chiefofstate.v1.tests.{Account, AccountOpened}
import com.namely.protobuf.chiefofstate.v1.writeside.{
  HandleEventRequest,
  HandleEventResponse,
  WriteSideHandlerServiceClient
}
import io.grpc.Status
import io.superflat.lagompb.protobuf.v1.core.MetaData
import org.scalamock.scalatest.MockFactory

import scala.concurrent.Future

class AggregateEventHandlerSpec extends TestSpec with MockFactory {

  "Chief-Of-State Event Handler" should {

    "handle event successfully as expected" in {
      val priorState: Any = Any.pack(Account.defaultInstance)
      val eventMeta: MetaData = MetaData.defaultInstance
      val accountId: String = UUID.randomUUID.toString
      val accountNumber: String = "123445"

      val stateProto: Seq[String] = Seq(Util.getProtoFullyQualifiedName(Any.pack(Account.defaultInstance)))
      val eventsProtos: Seq[String] =
        Seq(Util.getProtoFullyQualifiedName(Any.pack(AccountOpened.defaultInstance)))

      val handlerSetting: HandlerSetting = HandlerSetting(enableProtoValidations = true, stateProto, eventsProtos)

      val event = AccountOpened()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accountId)

      val resultingState = Account()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accountId)
        .withBalance(100)

      // let us create a mock instance of the handler service client
      val mockGrpcClient = mock[WriteSideHandlerServiceClient]

      (mockGrpcClient
        .handleEvent(_: HandleEventRequest))
        .expects(
          HandleEventRequest()
            .withEvent(Any.pack(event))
            .withCurrentState(priorState)
            .withMeta(Util.toCosMetaData(eventMeta))
        )
        .returning(
          Future.successful(
            HandleEventResponse()
              .withResultingState(Any.pack(resultingState))
          )
        )

      val eventHandler: AggregateEventHandler = new AggregateEventHandler(null, mockGrpcClient, handlerSetting)
      val result: Any = eventHandler.handle(Any.pack(event), priorState, eventMeta)
      result shouldBe (Any.pack(resultingState))
      result.unpack[Account] shouldBe resultingState
    }

    "handle event when event type is not specified in handler settings as expected" in {
      val priorState: Any = Any.pack(Account.defaultInstance)
      val eventMeta: MetaData = MetaData.defaultInstance
      val accouuntId: String = UUID.randomUUID.toString
      val accountNumber: String = "123445"

      val stateProto: Seq[String] = Seq("namely.rogue.state")
      val eventsProtos: Seq[String] = Seq(Util.getProtoFullyQualifiedName(priorState))

      val handlerSetting: HandlerSetting = HandlerSetting(enableProtoValidations = true, stateProto, eventsProtos)

      val event = AccountOpened()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)

      val resultingState = Account()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)
        .withBalance(100)

      // let us create a mock instance of the handler service client
      val mockGrpcClient = mock[WriteSideHandlerServiceClient]

      (mockGrpcClient
        .handleEvent(_: HandleEventRequest))
        .expects(
          HandleEventRequest()
            .withEvent(Any.pack(event))
            .withCurrentState(priorState)
            .withMeta(Util.toCosMetaData(eventMeta))
        )
        .returning(
          Future.successful(
            HandleEventResponse()
              .withResultingState(Any.pack(resultingState))
          )
        )

      val eventHandler: AggregateEventHandler = new AggregateEventHandler(null, mockGrpcClient, handlerSetting)
      a[Exception] shouldBe thrownBy(
        eventHandler.handle(Any.pack(event), priorState, eventMeta)
      )
    }

    "handle event when event protos validation is disabled in handler settings as expected" in {
      val priorState: Any = Any.pack(Account.defaultInstance)
      val eventMeta: MetaData = MetaData.defaultInstance
      val accouuntId: String = UUID.randomUUID.toString
      val accountNumber: String = "123445"

      val handlerSetting: HandlerSetting = HandlerSetting(enableProtoValidations = false, Seq.empty, Seq.empty)

      val event = AccountOpened()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)

      val resultingState = Account()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)
        .withBalance(100)

      // let us create a mock instance of the handler service client
      val mockGrpcClient = mock[WriteSideHandlerServiceClient]

      (mockGrpcClient
        .handleEvent(_: HandleEventRequest))
        .expects(
          HandleEventRequest()
            .withEvent(Any.pack(event))
            .withCurrentState(priorState)
            .withMeta(Util.toCosMetaData(eventMeta))
        )
        .returning(
          Future.successful(
            HandleEventResponse()
              .withResultingState(Any.pack(resultingState))
          )
        )

      val eventHandler: AggregateEventHandler = new AggregateEventHandler(null, mockGrpcClient, handlerSetting)

      noException shouldBe thrownBy(eventHandler.handle(Any.pack(event), priorState, eventMeta))
    }

    "handle event when event validation is enabled and the FQNs not provided in handler settings as expected" in {
      val priorState: Any = Any.pack(Account.defaultInstance)
      val eventMeta: MetaData = MetaData.defaultInstance
      val accouuntId: String = UUID.randomUUID.toString
      val accountNumber: String = "123445"

      val handlerSetting: HandlerSetting = HandlerSetting(enableProtoValidations = true, Seq.empty, Seq.empty)

      val event = AccountOpened()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)

      val resultingState = Account()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)
        .withBalance(100)

      // let us create a mock instance of the handler service client
      val mockGrpcClient = mock[WriteSideHandlerServiceClient]

      (mockGrpcClient
        .handleEvent(_: HandleEventRequest))
        .expects(
          HandleEventRequest()
            .withEvent(Any.pack(event))
            .withCurrentState(priorState)
            .withMeta(Util.toCosMetaData(eventMeta))
        )
        .returning(
          Future.successful(
            HandleEventResponse()
              .withResultingState(Any.pack(resultingState))
          )
        )

      val eventHandler: AggregateEventHandler = new AggregateEventHandler(null, mockGrpcClient, handlerSetting)

      a[Exception] shouldBe thrownBy(
        eventHandler.handle(Any.pack(event), priorState, eventMeta)
      )
    }

    "handle failed response as expected" in {
      val priorState: Any = Any.pack(Account.defaultInstance)
      val eventMeta: MetaData = MetaData.defaultInstance
      val accouuntId: String = UUID.randomUUID.toString
      val accountNumber: String = "123445"

      val stateProto: Seq[String] = Seq(Util.getProtoFullyQualifiedName(Any.pack(Account.defaultInstance)))
      val eventsProtos: Seq[String] =
        Seq(Util.getProtoFullyQualifiedName(Any.pack(AccountOpened.defaultInstance)))

      val handlerSetting: HandlerSetting = HandlerSetting(enableProtoValidations = true, stateProto, eventsProtos)

      val event = AccountOpened()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)

      // let us create a mock instance of the handler service client
      val mockGrpcClient = mock[WriteSideHandlerServiceClient]

      (mockGrpcClient
        .handleEvent(_: HandleEventRequest))
        .expects(
          HandleEventRequest()
            .withEvent(Any.pack(event))
            .withCurrentState(priorState)
            .withMeta(Util.toCosMetaData(eventMeta))
        )
        .returning(Future.failed(new GrpcServiceException(Status.INTERNAL)))

      val eventHandler: AggregateEventHandler = new AggregateEventHandler(null, mockGrpcClient, handlerSetting)
      a[Exception] shouldBe thrownBy(
        eventHandler.handle(Any.pack(event), priorState, eventMeta)
      )
    }

    "handle broken event handler as expected" in {
      val priorState: Any = Any.pack(Account.defaultInstance)
      val eventMeta: MetaData = MetaData.defaultInstance
      val accouuntId: String = UUID.randomUUID.toString
      val accountNumber: String = "123445"

      val stateProto: Seq[String] = Seq(Util.getProtoFullyQualifiedName(Any.pack(Account.defaultInstance)))
      val eventsProtos: Seq[String] =
        Seq(Util.getProtoFullyQualifiedName(Any.pack(AccountOpened.defaultInstance)))

      val handlerSetting: HandlerSetting = HandlerSetting(enableProtoValidations = true, stateProto, eventsProtos)

      val event = AccountOpened()
        .withAccountNumber(accountNumber)
        .withAccountUuid(accouuntId)

      // let us create a mock instance of the handler service client
      val mockGrpcClient = mock[WriteSideHandlerServiceClient]

      (mockGrpcClient
        .handleEvent(_: HandleEventRequest))
        .expects(
          HandleEventRequest()
            .withEvent(Any.pack(event))
            .withCurrentState(priorState)
            .withMeta(Util.toCosMetaData(eventMeta))
        )
        .throws(new RuntimeException("broken"))

      val eventHandler: AggregateEventHandler = new AggregateEventHandler(null, mockGrpcClient, handlerSetting)
      a[Exception] shouldBe thrownBy(
        eventHandler.handle(Any.pack(event), priorState, eventMeta)
      )
    }
  }
}