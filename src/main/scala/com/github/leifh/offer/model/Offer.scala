package com.github.leifh.offer.model

import java.time.Instant
import java.util.Currency

import io.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(description = "An offer object")
case class Offer(
  @(ApiModelProperty @field)(value = "A unique identifier for the pet")
  id: Option[Long],
  @(ApiModelProperty @field)(value = "A shopper friendly description")
  description: String,
  @(ApiModelProperty @field)(value = "the price of the offer")
  price: BigDecimal,
  @(ApiModelProperty @field)(value = "the currency of the price", dataType = "string")
  currency: Currency,
  @(ApiModelProperty @field)(value = "the date creation of the offer (ISO-8601)", dataType = "string")
  creationTime: Instant,
  @(ApiModelProperty @field)(value = "how long the offer is valid (in seconds)", dataType = "number")
  duration: java.time.Duration,
  @(ApiModelProperty @field)(value = "is this offer canceled")
  canceled:Boolean
) {
}

object Offer{
  def apply(description: String,
            price: Double,
            currency: String,
            duration: java.time.Duration): Offer = {
    Offer(None, description, price, Currency.getInstance(currency), Instant.now(), duration, false)
  }
}
