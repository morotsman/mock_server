package controllers

import akka.actor.ActorSystem
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
import play.api.libs.json._

@Singleton
class MockController @Inject() (actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends Controller {

  val mockActor = actorSystem.actorOf(MockActor.props, "mock-actor")
  val statisticsActor = actorSystem.actorOf(StatisticsActor.props, "statistics-actor")
  implicit val timeout: Timeout = 240.seconds

  def mock(name: String) = Action.async { request =>
    val startTime = System.currentTimeMillis
    val mockResource = MockResource(request.method, name)
    val result = (mockActor ? MockRequest(mockResource)).mapTo[MockSpec].map { spec => 
       spec match{
        case MockSpec(c,_,body) if c == 200=> Ok(body)
        case MockSpec(c,_,body) if c == 201=> Created(body)
        case MockSpec(c,_,body) if c == 203=> Accepted(body)
        case MockSpec(c,_,body) if c == 400=> BadRequest
        case MockSpec(c,_,body) if c == 409=> Conflict
        case MockSpec(c,_,body) if c == 413=> EntityTooLarge
        case MockSpec(c,_,body) if c == 417=> ExpectationFailed
        case MockSpec(c,_,body) if c == 403=> Forbidden
        case MockSpec(c,_,body) if c == 302=> Found(body)
        case MockSpec(c,_,body) if c == 410=> Gone
        case MockSpec(c,_,body) if c == 500=> InternalServerError
        case MockSpec(c,_,body) if c == 405=> MethodNotAllowed
        case MockSpec(c,_,body) if c == 301=> MovedPermanently(body)
        
        case MockSpec(c,_,body) if c == 204=> NoContent
        case MockSpec(c,_,body) if c == 203=> NonAuthoritativeInformation
        case MockSpec(c,_,body) if c == 406=> NotAcceptable
        case MockSpec(c,_,body) if c == 404=> NotFound
        case MockSpec(c,_,body) if c == 501=> NotImplemented
        case MockSpec(c,_,body) if c == 304=> NotModified
        case MockSpec(c,_,body) if c == 206=> PartialContent
        case MockSpec(c,_,body) if c == 412=> PreconditionFailed
        case MockSpec(c,_,body) if c == 408=> RequestTimeout
        case MockSpec(c,_,body) if c == 205=> ResetContent
        case MockSpec(c,_,body) if c == 303=> SeeOther(body)
        case MockSpec(c,_,body) if c == 503=> ServiceUnavailable
        
        case MockSpec(c,_,body) if c == 307=> TemporaryRedirect(body)
        case MockSpec(c,_,body) if c == 429=> TooManyRequests
        case MockSpec(c,_,body) if c == 401=> Unauthorized
        case MockSpec(c,_,body) if c == 415=> UnsupportedMediaType
        case MockSpec(c,_,body) if c == 414=> UriTooLong
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
    (mockActor ? ListMocks).mapTo[Set[MockResource]].map { msg => Ok(Json.toJson(msg)) }
  }

  def createMock(method: String, name: String) = Action.async(BodyParsers.parse.json) { request =>
    request.body.validate[MockSpec].map {
      mock => (mockActor ? AddMock(MockResource(method,name),mock)).mapTo[MockSpec].map { msg => Ok(Json.toJson(msg)) }
    }.recoverTotal {
      errors => Future.successful(BadRequest("Bad request: " + JsError.toFlatJson(errors)))
    }
  }
  
  def getMock(method: String, name: String) = Action.async { request =>
    (mockActor ? GetMock(MockResource(method,name))).mapTo[Option[MockSpec]].map { 
      case Some(msg) => Ok(Json.toJson(msg)) 
      case None => NotFound
    }
  } 
  
  def deleteMock(method: String, name: String) = Action.async { request =>
    (mockActor ? DeleteMock(MockResource(method,name))).map { msg => Ok }
  } 
  

}
