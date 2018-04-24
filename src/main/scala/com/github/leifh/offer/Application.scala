package com.github.leifh.offer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.github.leifh.offer.persistence.{H2OfferRepository, OfferRepository}
import com.github.leifh.offer.rest.OfferRestService
import com.github.leifh.offer.service.{OfferService, OfferServiceImpl}
import com.github.swagger.akka.SwaggerHttpService
import com.softwaremill.macwire._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import slick.jdbc.H2Profile.api._

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

case class AppConfig(host: String, port: Int, actorSystemName: String, devMode: Boolean)

object Application extends App{


  val logger = LoggerFactory.getLogger(Application.getClass)

  // read the application configuration
  val conf = ConfigFactory.load()
  val appConfigResult = pureconfig.loadConfig[AppConfig](conf.getConfig("application"))
  val appConfig = appConfigResult match {
    case Right(conf) => conf
    case Left(error) => throw new Exception(s"Error reading the configuration : $error")
  }

  // initialize akka
  implicit val actorSystem: ActorSystem = ActorSystem(appConfig.actorSystemName)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  // Dependencies injection
  lazy val offerRepository : OfferRepository = wire[H2OfferRepository]
  lazy val offerService : OfferService = wire[OfferServiceImpl]
  lazy val offerRestService : OfferRestService = wire[OfferRestService]

  val db = Database.forConfig("h2db")

  // create the database schema and insert some data
  if(appConfig.devMode) {
    val schema = H2OfferRepository.offers.schema

    db.run{
      DBIO.seq(
        schema.create
      )
    }
  }

  // Swagger configuration
  val swaggerUiRoute : Route = cors() {
    path("swagger") { getFromResource("swagger/index.html") } ~
      getFromResourceDirectory("swagger")
  }

  object SwaggerDocService extends SwaggerHttpService {
    override val apiClasses: immutable.Set[Class[_]] = immutable.Set(classOf[OfferRestService])
    override val host = s"${appConfig.host}:${appConfig.port}"
  }

  // the aggregation of all the services provided by the application
  val routes = offerRestService.routes ~ SwaggerDocService.routes ~ swaggerUiRoute


  val bindingFuture = Http().bindAndHandle(routes, appConfig.host, appConfig.port).map(
    binding => {
      logger.info(s"REST interface bound to ${binding.localAddress}")
    }
  )


  // force the shutdown of the application
  var stopped = false
  def stop(): Unit = synchronized {
    if(!stopped) {
      logger.info("Terminating...")
      db.close()
      actorSystem.terminate()
      Await.result(actorSystem.whenTerminated, 30 seconds)
      logger.info("Terminated... Bye")
      stopped = true
    }
  }

  // cleanup
  scala.sys.addShutdownHook {
    stop()
  }

}
