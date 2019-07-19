package controllers

import akka.actor.ActorSystem
import akka.actor.ActorRef
import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.concurrent.duration._
import akka.pattern.ask
import services.MockActor
import services.MockActor._
import services.StatisticsActor
import services.StatisticsActor._
import akka.util.Timeout
import model.MockSpec
import model.MockResource
import model.Mock
import play.api.libs.json._

@Singleton
class MockController @Inject() (@Named("statisticsActor") statisticsActor: ActorRef,
    @Named("mockActor") mockActor: ActorRef,
    actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends Controller {

  implicit val timeout: Timeout = 240.seconds

  def mock(path: String) = Action.async { request =>
    val startTime = System.currentTimeMillis
    val mockResource = MockResource(request.method, path, request.body.asText)
    statisticsActor ! IncomingRequest(mockResource)
    val result = (mockActor ? MockRequest(mockResource)).mapTo[Mock].map { mock =>
       mock match{
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 200=> Ok(body).as(contentType)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 201=> Created(body).as(contentType)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 203=> Accepted(body).as(contentType)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 400=> BadRequest
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 409=> Conflict
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 413=> EntityTooLarge
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 417=> ExpectationFailed
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 403=> Forbidden
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 302=> Found(body).as(contentType)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 410=> Gone
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 500=> InternalServerError
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 405=> MethodNotAllowed
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 301=> MovedPermanently(body).as(contentType)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 204=> NoContent
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 203=> NonAuthoritativeInformation
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 406=> NotAcceptable
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 404=> NotFound
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 501=> NotImplemented
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 304=> NotModified
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 206=> PartialContent
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 412=> PreconditionFailed
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 408=> RequestTimeout
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 205=> ResetContent
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 303=> SeeOther(body)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 503=> ServiceUnavailable
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 307=> TemporaryRedirect(body).as(contentType)
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 429=> TooManyRequests
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 401=> Unauthorized
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 415=> UnsupportedMediaType
        case Mock(_,_, MockSpec(c,_,body,contentType)) if c == 414=> UriTooLong
        case _ => ???
      }
    }
    result.foreach { x =>
      val endTime = System.currentTimeMillis
      statisticsActor ! CompletedRequest(mockResource,endTime-startTime)
    }
    result
  }

  def mockResources = Action.async {
    println("Controller: ListMocks")
    (mockActor ? ListMocks).mapTo[List[Mock]].map { msg => Ok(Json.toJson(msg)) }
  }

  def createMock() = Action.async(BodyParsers.parse.json) { request =>
    println("Controller, create mock: %s".format(request.body))
    request.body.validate[Mock].map { mock =>
      (mockActor ? AddMock(mock)).mapTo[Mock].map { msg => Ok(Json.toJson(msg)) }
    }.recoverTotal {
      errors => Future.successful(BadRequest("Bad request: " + JsError.toJson(errors)))
    }
  }

  def updateMock(id: Int) = Action.async(BodyParsers.parse.json) { request =>
    println("Controller, update mock: %s".format(request.body))
    request.body.validate[Mock].map { mock =>
      (mockActor ? UpdateMock(id, mock)).mapTo[Mock].map { msg => Ok(Json.toJson(msg)) }
    }.recoverTotal {
      errors => Future.successful(BadRequest("Bad request: " + JsError.toJson(errors)))
    }
  }

  def getMock(id: Int) = Action.async { request =>
    println("Controller: getMock")
    (mockActor ? GetMock(id)).mapTo[Option[Mock]].map {
      case Some(msg) => Ok(Json.toJson(msg))
      case None => NotFound
    }
  }

  def deleteMock(id: Int) = Action.async { request =>
    println("Controller: deleteMock")
    statisticsActor ! DeleteMock(id)
    (mockActor ? DeleteMock(id)).map { msg => Ok }
  }


}
