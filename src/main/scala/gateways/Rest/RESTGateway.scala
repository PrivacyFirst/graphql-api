package gateways.Rest

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import entities.Entity
import gateways.Gateway
import io.circe.Json
import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import scalaj.http.Http

import scala.concurrent.{ExecutionContext, Future}

abstract class RESTGateway[E <: Entity] extends Gateway[E] {
  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val executionContext: ExecutionContext = system.dispatcher
  implicit lazy val materializer: Materializer = ActorMaterializer()

  def jsonStringToEntity: String => Option[E] = jsonString => {
    parse(jsonString) match {
      case Right(json) => Option(jsonValToEntity(json))
      case _ => None
    }
  }
  def jsonValToEntity(json: Json):E

  def jsonListToEntityList(jsonString: String): Seq[E] = {
    parse(jsonString) match {
      case Right(json) => {
        val array = root.arr
        println(root.string.toString)
        array.getOption(json).get.map(jsonValToEntity)
      }
      case _ => Seq[E]()
    }
  }

  //def entityToJsonString: E => String
  def baseURL: String
  def route: String
  def parameters: Map[String, String]
  override def create(entity: E): Future[E] = {Future(entity)}

  override def createAll(entities: Seq[E]): Future[Seq[E]] = Future(entities)

  private def setupURL(selectorParameters: Seq[String] = Seq()): String = {
    val regex = """\{.*}""".r
    val resolved = regex.split(route).zipAll(selectorParameters,"","").map(tuple=> tuple._1 + tuple._2).fold("")((a,b)=>a+b)
    (baseURL+resolved).replaceAll(" ", "")
  }

  override def read(parameterValues:Map[String,String] = Map(), selectorParameters: Seq[String] = Seq()): Future[Option[E]] = Future {
    val resolved = setupURL(selectorParameters)
    val request = Http(resolved).params(parameters ++ parameterValues)
    jsonStringToEntity(request.asString.body)
  }

  override def readAll(parameterValues:Map[String,String] = Map(), selectorParameters: Seq[String] = Seq()): Future[Seq[E]] = Future {
    val resolved = setupURL(selectorParameters)
    println(resolved)
    val result = Http(resolved).params(parameters ++ parameterValues).asString
    jsonListToEntityList(result.body)
  }

  override def update(entity: E): Future[Option[E]] = Future(Option(entity))

  override def updateAll(entities: Seq[E]): Future[Seq[E]] = Future(entities)

  override def delete(entity: E): Future[E] = Future(entity)

  override def deleteAll(entities: Seq[E]): Future[Seq[E]] = Future(entities)
}
