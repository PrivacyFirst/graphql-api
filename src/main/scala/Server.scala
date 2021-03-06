import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.parser.DeliveryScheme.Try
import sangria.marshalling.circe._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._

import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import GraphQLRequestUnmarshaller._
import com.softwaremill.macwire.wire
import sangria.slowlog.SlowLog

object Server extends App with CorsSupport {
  implicit val system:ActorSystem  = ActorSystem("sangria-server")
  import system.dispatcher

  def executeGraphQL(query: Document, operationName: Option[String], variables: Json, tracing: Boolean) =
    complete(Executor.execute(GraphQLSchema.schema, query,
      variables = if (variables.isNull) Json.obj() else variables,
      operationName = operationName,
      middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil)
      .map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      })

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError =>
      Json.obj("errors" -> Json.arr(
        Json.obj(
          "message" -> Json.fromString(syntaxError.getMessage),
          "locations" -> Json.arr(Json.obj(
            "line" -> Json.fromBigInt(syntaxError.originalError.position.line),
            "column" -> Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) =>
      formatError(e.getMessage)
    case e =>
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(message))))
/*
*
parameters(Symbol("query"), Symbol("operationName").?, Symbol("variables").?) { (query, operationName, variables) =>
  QueryParser.parse(query) match {
    case Success(ast) =>
      variables.map(parse) match {
        case Some(Left(error)) => complete(BadRequest, formatError(error))
        case Some(Right(json)) => executeGraphQL(ast, operationName, json, tracing.isDefined)
        case None => executeGraphQL(ast, operationName, Json.obj(), tracing.isDefined)
      }
    case Failure(error) => complete(BadRequest, formatError(error))
  }
}*/
  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
      path("graphql") {
        get {
          explicitlyAccepts(`text/html`) {
            getFromResource("assets/playground.html")
          } ~
          parameters(Symbol("query"),Symbol("operationName").?, Symbol("variables").?) {
            (query, operationName, variables) => {
              QueryParser.parse(query) match {
                case Success(ast) =>
                  variables.map(parse) match {
                    case Some(Left(error)) => complete(BadRequest, formatError(error))
                    case Some(Right(json)) => executeGraphQL(ast, operationName, json, tracing.isDefined)
                    case None => executeGraphQL(ast, operationName, Json.obj(), tracing.isDefined)
                  }
                case Failure(error) => complete(BadRequest, formatError(error))
              }
            }
          }
        } ~
          post {
            parameters(Symbol("query").?, Symbol("operationName").?, Symbol("variables").?) { (queryParam, operationNameParam, variablesParam) =>
              entity(as[Json]) { body =>
                val query = queryParam orElse root.query.string.getOption(body)
                val operationName = operationNameParam orElse root.operationName.string.getOption(body)
                val variablesStr = variablesParam orElse root.variables.string.getOption(body)

                query.map(QueryParser.parse(_)) match {
                  case Some(Success(ast)) =>
                    variablesStr.map(parse) match {
                      case Some(Left(error)) => complete(BadRequest, formatError(error))
                      case Some(Right(json)) => executeGraphQL(ast, operationName, json, tracing.isDefined)
                      case None => executeGraphQL(ast, operationName, root.variables.json.getOption(body) getOrElse Json.obj(), tracing.isDefined)
                    }
                  case Some(Failure(error)) => complete(BadRequest, formatError(error))
                  case None => complete(BadRequest, formatError("No query to execute"))
                }
              } ~
                entity(as[Document]) { document =>
                  variablesParam.map(parse) match {
                    case Some(Left(error)) => complete(BadRequest, formatError(error))
                    case Some(Right(json)) => executeGraphQL(document, operationNameParam, json, tracing.isDefined)
                    case None => executeGraphQL(document, operationNameParam, Json.obj(), tracing.isDefined)
                  }
                }
            }
          }
      }
    } ~
      (get & pathEndOrSingleSlash) {
        redirect("/graphql", PermanentRedirect)
      }

  val PORT = sys.props.get("http.port").fold(9000)(_.toInt)
  val INTERFACE = "0.0.0.0"
  Http().newServerAt(INTERFACE, PORT).bindFlow(corsHandler(route))
}