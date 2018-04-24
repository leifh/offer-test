package com.github.leifh.offer.persistence

import java.sql.Timestamp
import java.util.Currency

import com.github.leifh.offer.model.Offer
import slick.jdbc.H2Profile.api._

/**
  * Define the operation on the database.
  */
trait OfferRepository {

  def get(id: Long) : DBIO[Option[Offer]]
  def getValid(id: Long) : DBIO[Option[Offer]]
  def cancel(id: Long) : DBIO[Int]
  def save(offer: Offer): DBIO[Long]
  def findAll() : DBIO[Seq[Offer]]
  def findAllValid() : DBIO[Seq[Offer]]
}

object H2OfferRepository {

  private def mapRow(tuple: (Option[Long],String,BigDecimal,String,Timestamp,Long,Boolean)): Offer = {
    Offer(
      tuple._1,
      tuple._2,
      tuple._3,
      Currency.getInstance(tuple._4),
      tuple._5.toInstant,
      java.time.Duration.ofSeconds(tuple._6),
      tuple._7
    )
  }

  private def unMapRow(offer:Offer) : Option[(Option[Long],String,BigDecimal,String,java.sql.Timestamp,Long,Boolean)] = {
    val tuple = (
      offer.id,
      offer.description,
      offer.price,
      offer.currency.getCurrencyCode,
      Timestamp.from(offer.creationTime),
      offer.duration.getSeconds,
      offer.canceled
    )

    Some(tuple)
  }

  /**
    * Table definition
    */
  class Offers(tag: Tag) extends Table[Offer](tag, "OFFERS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def description = column[String]("DESCRIPTION")
    def price = column[BigDecimal]("PRICE")
    def currency = column[String]("CURRENCY")
    def creationTime = column[java.sql.Timestamp]("creationTime")
    def duration = column[Long]("DURATION")
    def canceled = column[Boolean]("CANCELED")
    def * = (id.?, description, price, currency, creationTime, duration, canceled) <> (mapRow _, unMapRow _)
  }

  val offers = TableQuery[Offers]

  // H2 SQL Functions
  val dateAdd = SimpleFunction.ternary[String, Long, Timestamp, Timestamp]("DATEADD")
  val currentTimestamp = SimpleFunction.nullary[Timestamp]("CURRENT_TIMESTAMP")
}

/**
  * An implementation of the repository for H2
  */
class H2OfferRepository extends OfferRepository {
  import H2OfferRepository._

  override def save(offer: Offer) : DBIO[Long] = {
    val insertQuery = offers returning offers.map(_.id)

    insertQuery += offer
  }

  override def cancel(id: Long): DBIO[Int] = {
    val query = for{
      o <- offers if o.id === id
    } yield o.canceled

    query.update(true)
  }

  override def findAll() : DBIO[Seq[Offer]] = {
    offers.result
  }


  override def findAllValid(): DBIO[Seq[Offer]] = {

    val query = for{
      o <- offers if o.canceled === false && currentTimestamp < dateAdd("SECOND", o.duration, o.creationTime)
    } yield o

    query.result
  }
  override def get(id: Long): DBIO[Option[Offer]] = {
    offers.filter(_.id === id).result.headOption
  }

  override def getValid(id: Long): DBIO[Option[Offer]] = {

    val query = for{
      o <- offers if o.canceled === false && currentTimestamp < dateAdd("SECOND", o.duration, o.creationTime) && o.id === id
    } yield o

    query.result.headOption
  }
}
