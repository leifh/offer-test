package com.github.leifh.offer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit}
import com.github.leifh.offer.model.Offer
import com.github.leifh.offer.persistence.H2OfferRepository
import com.github.leifh.offer.rest.JsonSupport
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

class OfferServiceSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll with  BeforeAndAfter with JsonSupport {

  def createOffer(offer: Offer)(implicit ec: ExecutionContext) : Long = {
    val futureResponse = for {
      requestEntity <- Marshal(offer).to[RequestEntity]
      httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/offers", entity = requestEntity))
      stringResponse <- Unmarshal(httpResponse.entity).to[String]
    } yield (stringResponse, httpResponse.status, httpResponse.entity.contentType.toString())

    val r = Await.result(futureResponse, 2 seconds)
    val offerId = r._1.split(":")(1)

    offerId.toLong
  }

  override def beforeAll(): Unit = {
    // initialize the application
    Application.main(Array.empty)
  }

  override def afterAll {
    // cleanup
    Application.stop()
    TestKit.shutdownActorSystem(system)
  }

  before {
    // delete the table before each tests
    import slick.jdbc.H2Profile.api._
    val clear = Application.db.run {
      H2OfferRepository.offers.delete
    }
    Await.result(clear, 1 second)
  }


  implicit val executionContext = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val http = Http()

  "A Offer service" should {

    "create offer" in {

      val offer = Offer("an offer", 1.23, "CHF", java.time.Duration.ofSeconds(10))

      val futureResponse = for {
        requestEntity <- Marshal(offer).to[RequestEntity]
        httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/offers", entity = requestEntity))
        stringResponse <- Unmarshal(httpResponse.entity).to[String]
      } yield (stringResponse, httpResponse.status, httpResponse.entity.contentType.toString())

      Await.result(futureResponse, 10 seconds) should matchPattern {
        case (r: String, StatusCodes.Created, "text/plain; charset=UTF-8") if r.startsWith("Offer created:") =>
      }

    }

    "returns an existing valid offer" in {
      val offer = Offer("an offer1", 4.56, "CHF", java.time.Duration.ofSeconds(10))

      val offerId = createOffer(offer)

      val queryResponse = for {
        httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.GET, uri = s"http://localhost:8080/offers/$offerId"))
        offer <- Unmarshal(httpResponse.entity).to[Offer]
      } yield (offer, httpResponse.status, httpResponse.entity.contentType.toString())

      Await.result(queryResponse, 2 seconds) should matchPattern {
        case (Offer(_, "an offer1", _, _, _, _, _), StatusCodes.OK, "application/json") =>
      }
    }
  }


  "don't return an invalid offer" in {
    val offer = Offer("an offer2", 4.56, "CHF", java.time.Duration.ofSeconds(0))

    val offerId = createOffer(offer)

    val queryResponse = for {
      httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.GET, uri = s"http://localhost:8080/offers/$offerId"))
      offer <- Unmarshal(httpResponse.entity).to[String]
    } yield (offer, httpResponse.status, httpResponse.entity.contentType.toString())

    Await.result(queryResponse, 2 seconds) should matchPattern {
      case (_, StatusCodes.NotFound, _) =>
    }
  }

  "return only valid offers" in {

    val id1 = createOffer(Offer("an offer1", 4.56, "CHF", java.time.Duration.ofSeconds(10)))
    val id2 = createOffer(Offer("an offer2", 4.56, "CHF", java.time.Duration.ofSeconds(0)))
    val id3 = createOffer(Offer("an offer3", 4.56, "CHF", java.time.Duration.ofSeconds(1000)))

    val queryResponse = for {
      httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.GET, uri = s"http://localhost:8080/offers"))
      offers <- Unmarshal(httpResponse.entity).to[Seq[Offer]]
    } yield (offers, httpResponse.status, httpResponse.entity.contentType.toString())

    val response = Await.result(queryResponse, 2 seconds)
    response should matchPattern {
      case (offers : Seq[Offer], StatusCodes.OK, "application/json") if List(id1,id3).forall(i => offers.map(_.id.get).contains(i.toLong)) =>
    }
  }

  "cancel a valid offer" in {
    val id1 = createOffer(Offer("an offer1", 4.56, "CHF", java.time.Duration.ofSeconds(10)))

    val deleteResponse = for {
      httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.DELETE, uri = s"http://localhost:8080/offers/$id1"))
      message <- Unmarshal(httpResponse.entity).to[String]
    } yield (message, httpResponse.status, httpResponse.entity.contentType.toString())

    val r = Await.result(deleteResponse, 2 seconds)
    val offerId = r._1.split(":")(1)

    val queryResponse = for {
      httpResponse <- http.singleRequest(HttpRequest(method = HttpMethods.GET, uri = s"http://localhost:8080/offers/$offerId"))
      offer <- Unmarshal(httpResponse.entity).to[String]
    } yield (offer, httpResponse.status, httpResponse.entity.contentType.toString())

    Await.result(queryResponse, 2 seconds) should matchPattern {
      case (_, StatusCodes.NotFound, _) =>
    }
  }
}
