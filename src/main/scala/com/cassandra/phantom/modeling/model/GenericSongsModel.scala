package com.cassandra.phantom.modeling.model

import java.util.UUID

import com.cassandra.phantom.modeling.entity.Song
import com.websudos.phantom.dsl._

import scala.concurrent.Future

/**
  * Create the Cassandra representation of the Songs table
  */
class SongsModel extends CassandraTable[ConcreteSongsModel, Song] {

  override def tableName: String = "songs"

  object id extends TimeUUIDColumn(this) with PartitionKey[UUID] { override lazy val name = "song_id" }
  object artist extends StringColumn(this)
  object title extends StringColumn(this)

  override def fromRow(r: Row): Song = Song(id(r), title(r), artist(r))
}

/**
  * Define the available methods for this model
  */
abstract class ConcreteSongsModel extends SongsModel with RootConnector {

  def getBySongId(id: UUID): Future[Option[Song]] = {
    select
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .one()
  }

  def store(song: Song): Future[ResultSet] = {
    insert
      .value(_.id, song.id)
      .value(_.title, song.title)
      .value(_.artist, song.artist)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }

  def deleteById(id: UUID): Future[ResultSet] = {
    delete
      .where(_.id eqs id)
      .consistencyLevel_=(ConsistencyLevel.ONE)
      .future()
  }
}

