package actors


import akka.actor._
import actors.messages.MessagesActor._
import models.daos.{MultipleDAO, TransactionDAO, ProductDAO, OfferDAO}
import models.entities._
import akka.actor.Props
import play.api.libs.json.Json
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.pipe
import utilities.EconomyMath


import scala.concurrent.{Future, Await}

object UserActor{
  def props = Props[UserActor]
  def props(userId: Long,offerDAO: OfferDAO,productDAO: ProductDAO,transactionDAO: TransactionDAO,multipleDAO: MultipleDAO) =
    Props(classOf[UserActor],userId,offerDAO,productDAO,transactionDAO,multipleDAO)
}
class UserActor(userId: Long, offerDAO: OfferDAO,productDAO: ProductDAO,transactionDAO: TransactionDAO, multipleDAO: MultipleDAO) extends Actor with Stash{
  import context._
  println(self.path)
  var offerSender: (Long,ActorRef) = (0,null)


  def getOffer(offerId: Long):Future[Option[Offer]] ={
    offerDAO.byId(offerId).flatMap{
      case x => Future(x)
    }
  }
  def processOffer(o: Offer) ={
    val otherUserId= o.wantedUserId
    val product = Await.result(getUserProduct(userId,o.offProductId),1000 milli)
    if(product.productQuantity >= o.offAmount){
      becomeAndWatchTime(waitingResponse)
      context.actorSelection(s"../$otherUserId") ! Petition(userId,o.offProductId,o)

    }
    else{
      takeOfferSender ! TransactionError(userId,"No tienes suficiente producto")
    }

  }

  def getUserProduct(id: Long, pTypeId: Long)={ productDAO.byUserIdAndProductTypeId(id,pTypeId).flatMap{
    case Some(room) => Future(room)

    case _ => Future(null)
  }
  }

  def becomeAndWatchTime( receive: Actor.Receive) ={
    system.scheduler.scheduleOnce(5000 milliseconds) {
      self ! TimeOutMsg()
    }
    become(receive)

  }
  def unbecomeAndUnstash(): Unit ={
    unbecome()
    unstashAll()
  }


  def processPetition(p: Petition):TransactionCompleted ={
    val user1product= Await.result(getUserProduct(p.userId,p.offer.offProductId),1000 milli)
    val user1otherProduct= Await.result(getUserProduct(p.userId,p.offer.wantedProductId),1000 milli)
    val user2product =  Await.result(getUserProduct(userId,p.offer.offProductId),1000 milli)
    val user2otherProduct = Await.result(getUserProduct(userId,p.offer.wantedProductId),1000 milli)
    if(user1otherProduct == null | user1product == null |user2otherProduct == null | user2product == null){
      TransactionError(p.offer.wantedUserId,"Algun usuario no tiene un tipo de producto")
    }
    println(user2otherProduct)
    if(user2otherProduct.productQuantity >= p.amount){
            becomeAndWatchTime(waitingResponse)
            val product1 = user1product.copy(productQuantity = user1product.productQuantity - p.offer.offAmount)
            val product2 = user2otherProduct.copy(productQuantity = user2otherProduct.productQuantity - p.amount)
            val product3 = user2product.copy(productQuantity = user2product.productQuantity + p.offer.offAmount)
            val product4 = user1otherProduct.copy(productQuantity = user1otherProduct.productQuantity + p.amount)

            val newTransaction = Transaction(0,"test",p.userId,p.offer.offProductId,p.offer.offAmount,EconomyMath.RMS(product1,product4),this.userId,p.offer.wantedProductId,p.offer.wantedAmount,EconomyMath.RMS(product3,product2))
            Await.result(multipleDAO.completeTransaction(product1,product2,product3,product4,newTransaction,p.offer).map{re =>{

              unbecomeAndUnstash()
              TransactionSuccessfully(p.offer.wantedUserId)}},1000 milli)
    }
    else{
      TransactionError(p.offer.wantedUserId,"el user no tiene cantidad deseada")
    }

  }

  def matchThis(msg: Any,theSender: ActorRef) = msg match{
    case c:CreateOffer => {
      println(c.userId+ "  "+ c.givesProductId )
      val offer = Offer(0, c.marketId, c.wantsProductId, c.wantsAmount, c.userId, c.givesProductId, c.givesAmount)
      val product = Await.result(getUserProduct(c.userId,c.givesProductId), 1000 milli)
      if (product.productQuantity >= c.givesAmount) {
        offerDAO.insert(offer).onSuccess {
          case o: Long => {
            println("Yea")
            theSender ! OfferCreationOK(offer)
          }
          case _ =>
            theSender ! OfferCreationError("Error al crear oferta")
        }

      }
      else {
        theSender ! OfferCreationError("Producto insuficiente")
      }
    }
    case p:GetProducts =>{
      productDAO.byUser(userId) map {r => theSender ! r

      }}
    case _=>

  }


  var takeOfferSender:ActorRef = null

  def receive = {
    case tOffer:TakeOffer =>
    {
       val offer = Await.result(getOffer(tOffer.offerId),1000 milli)
      println(this.userId)
      offer match{
        case Some(o) => //Existia esta oferta
          offerSender = (o.wantedUserId,sender)
          takeOfferSender = sender();
          processOffer(o)
        case None => sender ! TransactionError(userId,"No existe oferta")

      }
    }
    case p:Petition =>
      val thesender = sender();
      thesender ! processPetition(p)

    case msg =>
      matchThis(msg,sender)



  }

  def waitingResponse: Receive ={


    case v:TransactionCompleted => //Me llego un mensaje diciendo que la transaccion se termino de forma correcta, por lo tanto proceso los mensajes anteriores de ofertas
      unbecome()
      unstashAll()
      if(v.userId == offerSender._1){
        val userid = v.userId
        takeOfferSender ! v
      }
      else{
        val userid = v.userId
        println(s"err user: $userid")
      }
    case d:DeadUser => //Usuario murio, proceso los mensajes anteriores
      if(d.userId == offerSender._1){
        unbecome()
        unstashAll()
        offerSender._2 ! "Error"
      }
      else{
        context.parent ! "error deadleter?"
      }
    case out:TimeOutMsg =>
      unbecome()
      unstashAll()
      offerSender._2 ! "TimeOut"
    case v:TakeOffer => //
      stash()
    case p:Petition => //Idem anterior
      stash()
    case msg=>
     matchThis(msg,sender)
  }


}