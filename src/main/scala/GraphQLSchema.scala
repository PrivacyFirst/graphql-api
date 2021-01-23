import sangria.schema._
import com.softwaremill.macwire._


/**
  * Defines a GraphQL schema for the current project
  */
object GraphQLSchema {
  val query = ObjectType("Query", fields(List(): _*))
  val mutation = Some(
    ObjectType("Mutation", fields(List(): _*))
  )
  val schema = Schema(query)
}
