package schemas
import sangria.schema._

trait Schema {
  def queries: List[Field[Unit, Unit]]
  def mutations: List[Field[Unit, Unit]]
}
