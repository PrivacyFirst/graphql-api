package gateways

import entities.Entity

import scala.concurrent.Future

trait Gateway[T <: Entity] {
  def create(entity: T): Future[T]
  def createAll(entities: Seq[T]): Future[Seq[T]]

  def read(parameters:Map[String,String] = Map(), selectorParameters: Seq[String] = Seq()): Future[Option[T]]
  def readAll(parameters:Map[String,String] = Map(), selectorParameters: Seq[String] = Seq()): Future[Seq[T]]

  def update(entity: T): Future[Option[T]]
  def updateAll(entities: Seq[T]): Future[Seq[T]]

  def delete(entity: T): Future[T]
  def deleteAll(entities: Seq[T]): Future[Seq[T]]
}
