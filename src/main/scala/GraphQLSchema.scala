import gateways.Rest.Polygon.ExchangeGateway
import resolvers.StocksResolver
import sangria.schema._
import schemas.PolygonSchema
import com.softwaremill.macwire._


/**
  * Defines a GraphQL schema for the current project
  */
object GraphQLSchema {
  lazy val stocksSchema = wire[PolygonSchema]

  val query = ObjectType("Query", fields(List(): _*))
  val mutation = Some(
    ObjectType("Mutation", fields(List(): _*))
  )
  val schema = Schema(query)
}
