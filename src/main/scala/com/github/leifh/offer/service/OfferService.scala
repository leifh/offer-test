package com.github.leifh.offer.service

import java.time.Instant

import com.github.leifh.offer.model.Offer
import com.github.leifh.offer.persistence.OfferRepository
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}

trait OfferService {

  /**
    * Return all valid offers in the system (not cancelled or expired)
    * @return a sequence of offers
    */
  def getValidOffers() : Future[Seq[Offer]]

  /**
    * Return all offers in the system.
    * @return a sequence of offers
    */
  def getOffers() : Future[Seq[Offer]]

  /**
    * Return a offer
    * @param id the id of the offer.
    * @return an offer or None if not found
    */
  def getOffer(id: Long) : Future[Option[Offer]]

  /**
    * Return a valid offer (not cancelled or expired)
    * @param id the id of the offer.
    * @return an offer or None if not found
    */
  def getValidOffer(id: Long) : Future[Option[Offer]]

  /**
    * Cancel an offer
    * @param id the id of the offer
    * @return True if the offer is canceled other False if the offer is not existing.
    */
  def cancelOffer(id: Long)(implicit executor: ExecutionContext) : Future[Boolean]

  /**
    * Create a new offer in the system.
    * @param offer an offer.
    * @return the id of the new offer.
    */
  def createOffer(offer: Offer) : Future[Long]
}

class OfferServiceImpl(offerRepository: OfferRepository, db: Database) extends OfferService{

  /**
    * Return all valid offers in the system (not cancelled or expired)
    * @return a sequence of offers
    */
  def getValidOffers() : Future[Seq[Offer]] = {
    db.run(offerRepository.findAllValid())
  }

  /**
    * Return all offers in the system.
    * @return a sequence of offers
    */
  def getOffers() : Future[Seq[Offer]] = {
    db.run(offerRepository.findAll())
  }

  /**
    * Return a offer
    * @param id the id of the offer.
    * @return an offer or None if not found
    */
  def getOffer(id: Long) : Future[Option[Offer]] = {
    db.run(offerRepository.get(id))
  }

  /**
    * Return a valid offer (not cancelled or expired)
    * @param id the id of the offer.
    * @return an offer or None if not found
    */
  def getValidOffer(id: Long) : Future[Option[Offer]] = {
    db.run(offerRepository.getValid(id))
  }

  /**
    * Cancel an offer
    * @param id the id of the offer
    * @return True if the offer is canceled other False if the offer is not existing.
    */
  def cancelOffer(id: Long)(implicit executor: ExecutionContext) : Future[Boolean] = {
    db.run(offerRepository.cancel(id)).map(_ == 1)
  }

  /**
    * Create a new offer in the system.
    * @param offer an offer.
    * @return the id of the new offer.
    */
  def createOffer(offer: Offer) : Future[Long] = {
    // use server time for the creation date
    val off = offer.copy(creationTime = Instant.now())
    db.run(offerRepository.save(off))
  }
}
