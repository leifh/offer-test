package com.github.leifh.offer.rest

import java.time.{Duration, Instant}
import java.util.Currency

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.leifh.offer.model.Offer
import spray.json.{DefaultJsonProtocol, JsNumber, JsString, JsValue, JsonFormat, RootJsonFormat}

/**
  * A trait providing mapping between scala or java object and the json format.
  */
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol{

  implicit object CurrencyJsonFormat extends RootJsonFormat[Currency] {
    override def write(obj: Currency): JsValue = JsString(obj.getCurrencyCode)

    override def read(json: JsValue): Currency = Currency.getInstance(json.convertTo[String])
  }

  implicit object LocalDateTimeJsonFormat extends RootJsonFormat[Instant] {

    override def write(obj: Instant): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Instant = Instant.parse(json.convertTo[String])
  }


  implicit object DurationJsonFormat extends RootJsonFormat[Duration] {
    override def write(obj: Duration): JsValue = JsNumber(obj.getSeconds)

    override def read(json: JsValue): Duration = Duration.ofSeconds(json.convertTo[Long])
  }

  implicit val offerFormat : RootJsonFormat[Offer] = jsonFormat7(Offer.apply)
}

