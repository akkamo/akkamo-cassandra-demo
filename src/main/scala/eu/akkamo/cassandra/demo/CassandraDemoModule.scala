package eu.akkamo.cassandra.demo

import akka.event.LoggingAdapter
import com.cassandra.phantom.modeling.entity.Song
import com.cassandra.phantom.modeling.model.ConcreteSongsModel
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.database.DatabaseImpl
import com.websudos.phantom.dsl._
import eu.akkamo
import eu.akkamo._
import eu.akkamo.cassandra.CassandraModule

import scala.concurrent.{Await, Future}
import scala.util.Try

class CassandraDemoModule extends akkamo.Module with akkamo.Runnable {
  override def dependencies(dependencies: Dependency): Dependency = dependencies
    .&&[LogModule].&&[CassandraModule]

  override def run(ctx: Context): Res[Context] = Try {
    val log: LoggingAdapter = ctx.get[LoggingAdapterFactory].apply(getClass)

    log.info("Starting Cassandra Demo application")
    doDemo(ctx.get[KeySpaceDef])
    ctx
  }


  def doDemo(akkamoConnector: KeySpaceDef): Unit = {
    val song = Song(UUIDs.timeBased(), "What a Wonderful World", "Louis Armstrong")
    val service = new SongsService(akkamoConnector)

    service.createTable()

    service.saveOrUpdate(song).map(result => {
      println(result)
      service.getSongById(song.id).map(songs => {
        println(s"Song from database ${songs}")
        service.dropTable()
      })
    })
  }

  class SongsDatabase(override val connector: KeySpaceDef) extends DatabaseImpl(connector) {

    object songsModel extends ConcreteSongsModel with connector.Connector

    object database extends SongsDatabase(connector)

  }

  class SongsService(override val connector: KeySpaceDef) extends SongsDatabase(connector) {

    def createTable(): Unit = {
      import scala.concurrent.duration._
      Await.result(autocreate().future(), 5000 millis)
    }

    def dropTable(): Unit = {
      import scala.concurrent.duration._
      Await.result(autodrop().future(), 5000 millis)
    }

    def saveOrUpdate(songs: Song): Future[ResultSet] = {
      database.songsModel.store(songs)
    }

    def getSongById(id: UUID): Future[Option[Song]] = {
      database.songsModel.getBySongId(id)
    }


  }


}


