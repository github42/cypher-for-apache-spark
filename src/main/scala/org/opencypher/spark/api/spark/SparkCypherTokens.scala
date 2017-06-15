package org.opencypher.spark.api.spark

import org.opencypher.spark.api.ir.global._

// Lightweight wrapper around token registry to expose a simple lookup api for all tokens that may occur in a data frame
final case class SparkCypherTokens(registry: TokenRegistry) {

  def labelName(id: Int): String = registry.label(LabelRef(id)).name
  def labelId(name: String): Int = registry.labelRefByName(name).id

  def relTypeName(id: Int): String = registry.relType(RelTypeRef(id)).name
  def relTypeId(name: String): Int = registry.relTypeRefByName(name).id

  def withLabel(name: String) = copy(registry = registry.withLabel(Label(name)))
  def withRelType(name: String) = copy(registry = registry.withRelType(RelType(name)))
}

object SparkCypherTokens {
  val empty = SparkCypherTokens(TokenRegistry.empty)
}
