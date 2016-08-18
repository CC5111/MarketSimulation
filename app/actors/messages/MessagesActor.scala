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
  case class TransactionSuccessfully(val userId: Long) extends TransactionCompleted
  case class TransactionError(val userId: Long) extends TransactionCompleted
  case class Petition(userId: Long,otherProductId: Long, offer: Offer){
    val amount = offer.wantedAmount
    val productId = offer.wantedProductId
  }
  case class GetAllOffers()
  case class DeadUser(userId: Long)
  case class TimeOutMsg()

}
