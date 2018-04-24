package com.github.leifh.offer.rest

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.github.leifh.offer.model.Offer
import com.github.leifh.offer.service.OfferService
import io.swagger.annotations._
import javax.ws.rs.Path

import scala.util.{Failure, Success}

@Api(value = "/offers", description = "Operations about offers")
@Path("/offers")
class OfferRestService(offerService: OfferService) extends JsonSupport{

  @Path("/{id}")
  @ApiOperation(
    httpMethod = "GET",
    value = "Returns a valid offer based on a ID",
    response = classOf[Offer]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "offerId", value = "The ID of an existing offer", required = false, dataType = "long", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 404, message = "Offer not found"),
    new ApiResponse(code = 500, message = "An error occurred:")
  ))
  def offersIdGetRoute : Route = cors() {
    path("offers" / LongNumber) { id =>
      extractExecutionContext { implicit executor =>
        get {
          onComplete(offerService.getValidOffer(id)) {
            case Success(Some(offer)) => complete(offer)
            case Success(None) => complete(StatusCodes.NotFound)
            case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(
    httpMethod = "DELETE",
    value = "Cancel an offer",
    response = classOf[String]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "offerId", value = "The ID of an existing offer", required = false, dataType = "long", paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Offer canceled:1"),
    new ApiResponse(code = 404, message = "Offer not found"),
    new ApiResponse(code = 500, message = "An error occurred: ...")
  ))
  def offersIdDeleteRoute: Route = cors() {
    path("offers" / LongNumber) { id =>
      extractExecutionContext { implicit executor =>
        delete {
          onComplete(offerService.cancelOffer(id)) {
            case Success(true) => complete(s"Offer canceled:$id")
            case Success(false) => complete((StatusCodes.NotFound, "Offer not found"))
            case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(
    httpMethod = "GET",
    value = "Get all valid offers",
    response = classOf[Offer],
    responseContainer = "List"
  )
  @ApiImplicitParams(Array(
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "An error occurred:")
  ))
  def offersGetRoutes : Route = cors() {
    path("offers") {
      get {
        onComplete(offerService.getValidOffers()) {
          case Success(offers) =>
            complete(offers)
          case Failure(ex) =>
            complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
        }
      }
    }
  }

  @Path("")
  @ApiOperation(
    httpMethod = "POST",
    value = "Create an offers",
    code = 201
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(value = "An offer to create", required = true, dataTypeClass = classOf[Offer], paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Offer created:1"),
    new ApiResponse(code = 500, message = "An error occurred: ...")
  ))
  def offersPostRoute : Route = cors() {
    path("offers") {
      post {
        entity(as[Offer]) { offer =>
          onComplete(offerService.createOffer(offer)) {
            case Success(generatedId) =>
              complete((StatusCodes.Created, s"Offer created:$generatedId"))
            case Failure(ex) =>
              complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        }
      }
    }
  }

  val routes: Route = offersIdGetRoute ~ offersGetRoutes ~ offersPostRoute ~ offersIdDeleteRoute
}
