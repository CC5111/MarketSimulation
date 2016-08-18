package actors


import akka.actor._
import actors.messages.MessagesActor._
import models.daos.{MultipleDAO, TransactionDAO, ProductDAO, OfferDAO}
import models.entities._
import akka.actor.Props
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.pipe


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
    println("me shego la oferta")
    val otherUserId= o.wantedUserId
    val product = Await.result(getUserProduct(o.offProductId),1000 milli)
    println("encontre el producto del usuario")
    if(product.productQuantity >= o.offAmount){
      println(s"el usuario tiene produsto $otherUserId")
      println (context.actorSelection(s"../$otherUserId").toString())
      context.actorSelection(s"../$otherUserId") ! Petition(userId,o.offProductId,o)

    }
    else{
      "No tiene suficiente producto el usuario"
    }

  }

  def getUserProduct(id: Long)={ productDAO.byId(id).flatMap{
    case Some(room) => Future(room)

    case _ => Future(null)
  }
  }


  def processPetition(p: Petition):TransactionCompleted ={
    println("intento de obtener el producto de este user")
    val product= Await.result(getUserProduct(p.productId),1000 milli)
    println("intento de obtener el producto del otro user")
    val otherProduct = Await.result(getUserProduct(p.otherProductId),1000 milli)
    if(product.productQuantity >= p.amount){
      println("el user tiene la cantidad buena")
            val product1 = product.copy(productQuantity = product.productQuantity - p.amount)
            val product2 = otherProduct.copy(productQuantity = otherProduct.productQuantity - p.offer.offAmount)
            val newTransaction = Transaction(0,"test",p.userId,p.offer.offProductId,p.offer.offAmount,0.0,this.userId,p.offer.wantedProductId,p.offer.wantedAmount,0.0)
            Await.result(multipleDAO.completeTransaction(product1,product2,newTransaction,p.offer).map{re =>  TransactionSuccessfully(p.offer.wantedUserId)},1000 milli)
    }
    else{
      println("el user no tiene cantidad deseada")
      TransactionError(p.offer.wantedUserId)
    }

  }

  def matchThis(msg: Any) = msg match{
    case _ => "TODO"
    /*case CreateOffer =>
    case DeleteOffer =>
    case GetBienestar =>
    case GetUtilidadMarginal =>*/
  }

  var takeOfferSender:ActorRef = null

  def receive = {
    case tOffer:TakeOffer =>
    {
       val offer = Await.result(getOffer(tOffer.offerId),1000 milli)
      offer match{
        case Some(o) => //Existia esta oferta
          offerSender = (o.wantedUserId,sender)
          takeOfferSender = sender();
          become(waitingResponse)
          system.scheduler.scheduleOnce(5000 milliseconds) {
            self ! TimeOutMsg()
          }
          sender ! processOffer(o)
        case None => sender ! "No existe oferta"

      }
    }
    case p:Petition =>
      println("me shego una petition")
      val thesender = sender();
      thesender ! processPetition(p)

    case msg =>
      print("become ")
      println(msg.toString)



  }

  def waitingResponse: Receive ={


    case v:TransactionCompleted => //Me llego un mensaje diciendo que la transaccion se termino de forma correcta, por lo tanto proceso los mensajes anteriores de ofertas
      if(v.userId == offerSender._1){
        unbecome()
        unstashAll()
        val userid = v.userId
        println(s"user: $userid")
        println(takeOfferSender.toString())
        pipe(Future("hola!")) to  context.parent
        context.parent ! "hola"

      }
      else{
        val userid = v.userId
        println(s"err user: $userid")
        pipe(Future("hola!")) to  context.parent
        context.parent ! "hola"
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
      println("unbecome")
      unbecome()
      unstashAll()

      offerSender._2 ! "TimeOut"


    case v:TakeOffer => //
      println("Estoy procesando una oferta, por lo tanto no puedo recibir mensajes")
      stash()
    case p:Petition => //Idem anterior
      stash()
    case msg=>
      print("unbecome: ")
      println(msg.toString)
     /* val st = msg.toString
      println (s"becomed $st")
      sender ! matchThis(msg)*/
  }


}