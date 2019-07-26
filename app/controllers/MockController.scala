package controllers

import akka.actor.ActorSystem
import akka.actor.ActorRef
import javax.inject._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._
import akka.pattern.ask
import services.MockActor._
import services.StatisticsActor._
import akka.util.Timeout
import model.{Matcher, Mock, MockResource, MockSpec}
import play.api.libs.json._

@Singleton
class MockController @Inject()(@Named("statisticsActor") statisticsActor: ActorRef,
                               @Named("mockActor") mockActor: ActorRef,
                               actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends Controller {

  implicit val timeout: Timeout = 240.seconds

  def mock(path: String): Action[AnyContent] = Action.async { request =>
    println("Received call")
    val startTime = System.currentTimeMillis
    val mockResource = MockResource(request.method, path)
    val matcher = Matcher(mockResource, request.body.asText)
    statisticsActor ! IncomingRequest(mockResource)
    val result = (mockActor ? matcher).mapTo[MockSpec].map { mock => {
      val MockSpec(httpStatus, _, body, contentType) = mock
      if (httpStatus == 200) Ok(body).as(contentType)
      else if (httpStatus == 201) Created(body).as(contentType)
      else if (httpStatus == 203) Accepted(body).as(contentType)
      else if (httpStatus == 400) BadRequest
      else if (httpStatus == 409) Conflict
      else if (httpStatus == 413) EntityTooLarge
      else if (httpStatus == 417) ExpectationFailed
      else if (httpStatus == 403) Forbidden
      else if (httpStatus == 302) Found(body).as(contentType)
      else if (httpStatus == 410) Gone
      else if (httpStatus == 500) InternalServerError
      else if (httpStatus == 405) MethodNotAllowed
      else if (httpStatus == 301) MovedPermanently(body).as(contentType)
      else if (httpStatus == 204) NoContent
      else if (httpStatus == 203) NonAuthoritativeInformation
      else if (httpStatus == 406) NotAcceptable
      else if (httpStatus == 404) NotFound
      else if (httpStatus == 501) NotImplemented
      else if (httpStatus == 304) NotModified
      else if (httpStatus == 206) PartialContent
      else if (httpStatus == 412) PreconditionFailed
      else if (httpStatus == 408) RequestTimeout
      else if (httpStatus == 205) ResetContent
      else if (httpStatus == 303) SeeOther(body)
      else if (httpStatus == 503) ServiceUnavailable
      else if (httpStatus == 307) TemporaryRedirect(body).as(contentType)
      else if (httpStatus == 429) TooManyRequests
      else if (httpStatus == 401) Unauthorized
      else if (httpStatus == 415) UnsupportedMediaType
      else if (httpStatus == 414) UriTooLong
      else ???
    }

    }
    result.foreach { x =>
      val endTime = System.currentTimeMillis
      statisticsActor ! CompletedRequest(mockResource, endTime - startTime)
    }
    result
  }

  def mockResources: Action[AnyContent] = Action.async {
    println("Controller: ListMocks")
    (mockActor ? ListMocks).mapTo[List[Mock]].map { msg => Ok(Json.toJson(msg)) }
  }

  def createMock(): Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    println("Controller, create mock: %s".format(request.body))
    request.body.validate[Mock].map { mock =>
      (mockActor ? AddMock(mock)).mapTo[Mock].map { msg => Ok(Json.toJson(msg)) }
    }.recoverTotal {
      errors => Future.successful(BadRequest("Bad request: " + JsError.toJson(errors)))
    }
  }

  def updateMock(id: Int): Action[JsValue] = Action.async(BodyParsers.parse.json) { request =>
    println("Controller, update mock: %s".format(request.body))
    request.body.validate[Mock].map { mock =>
      (mockActor ? UpdateMock(id, mock)).mapTo[Mock].map { msg => Ok(Json.toJson(msg)) }
    }.recoverTotal {
      errors => Future.successful(BadRequest("Bad request: " + JsError.toJson(errors)))
    }
  }

  def getMock(id: Int): Action[AnyContent] = Action.async { request =>
    println("Controller: getMock")
    (mockActor ? GetMock(id)).mapTo[Option[Mock]].map {
      case Some(msg) => Ok(Json.toJson(msg))
      case None => NotFound
    }
  }

  def deleteMock(id: Int): Action[AnyContent] = Action.async { request =>
    println("Controller: deleteMock")
    statisticsActor ! DeleteMock(id)
    (mockActor ? DeleteMock(id)).map { msg => Ok }
  }


}
