package actors.messages

import akka.actor.ActorRef
import models.entities.Offer

/**
 * Created by Nicolas on 01-08-2016.
 */
object MessagesActor {
  case class TakeOffer(marketId: Long,userId: Long, offerId: Long)
  case class OffQuantityOK(actorRef: ActorRef,productInterchange: TakeOffer)
  case class WantedQuantityOK(actorRef: ActorRef,productInterchange: TakeOffer)
  trait TransactionCompleted{
    val userId: Long
  }
  case class TransactionSuccessfully(userId: Long) extends TransactionCompleted
  case class TransactionError(userId: Long,error: String) extends TransactionCompleted
  case class Petition(userId: Long,otherProductId: Long, offer: Offer){
    val amount = offer.wantedAmount
    val productId = offer.wantedProductId
  }
  case class OfferCreationOK(offer: Offer)
  case class OfferCreationError(error: String)
  case class GetAllOffers(marketId: Long)
  case class CreateOffer(userId: Long, marketId: Long, wantsProductId: Long, wantsAmount: Long,givesProductId: Long, givesAmount: Long)
  case class DeadUser(userId: Long)
  case class TimeOutMsg()
  case class GetProducts(userId: Long)

}
