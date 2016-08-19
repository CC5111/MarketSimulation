package actors

import actors.messages.MessagesActor._
import akka.actor._

import models.daos._
import models.entities._
import play.api.libs.json.Json

object MarketActor{
  def props = Props[MarketActor]
  def props(marketId: Long,name: String,userDAO: UserDAO,offerDAO: OfferDAO,productDAO: ProductDAO,transactionDAO: TransactionDAO,multipleDAO: MultipleDAO) =
    Props(classOf[MarketActor],marketId,name,userDAO,offerDAO,productDAO,transactionDAO,multipleDAO)
}
class MarketActor(marketId: Long,name: String,userDAO: UserDAO,offerDAO:OfferDAO,productDAO:ProductDAO,transactionDAO:TransactionDAO,multipleDAO: MultipleDAO) extends Actor with ImplicitsModels{

  import context._
  import Actor._
  import akka.pattern.ask
  import scala.concurrent.duration._
  import akka.util.Timeout

  implicit val timeout: Timeout = 10.seconds
  println(self.path)
  val initUsers ={
    println("init busqueda de actors hijos")
    userDAO.byMarket(marketId).onSuccess{
      case users:Seq[User] =>
        println("users encontrados")
        users.foreach( user=>  context.actorOf(UserActor.props(user.id,offerDAO,productDAO,transactionDAO,multipleDAO),user.id.toString))

      case _ =>
        println("no encontre actores :C")

    }

  }

  def calculateBienestar(products: Seq[Product]):Double={
    var totalsum =0.0
    products.foreach(
    product =>
      totalsum += product.productConstant*Math.pow(product.productQuantity,product.productExponential)

    )
    totalsum
  }

  def sendAllBienestar()= {
    productDAO.all map{ x =>
      var sumatory = 0.0
      x.groupBy{_.id}.foreach( each =>
      sumatory+= calculateBienestar(each._2)
      )
      context.actorSelection("/user/*/flowActor")! sumatory

    }


  }

  def receive = {
    case t:TakeOffer => {
      val oldSender = sender
      val userid = t.userId
      println("envio el mensaje")
      (system.actorSelection(s"/user/market_$marketId/$userid") ? t).mapTo[Any].map { message =>
        println("YEAAAAAH " + message.toString)
        oldSender ! (message match {
          case v:TransactionSuccessfully=>{
             sendAllBienestar()
            Json.obj("status" ->"OK"  )}
          case v:TransactionError =>
            Json.obj("status" ->"KO","error" ->v.error)
          })
        }
      }
    case g:GetAllOffers =>
      println("getalloffers oli ")
      val oldSender = sender()
      offerDAO.byMarket(g.marketId).onSuccess{
        case offers:Seq[Offer] => {
          println("Yea")
          oldSender ! Json.obj("status" -> "OK", "content" -> Json.toJson(offers))
        }
        case _ =>
          oldSender ! Json.obj("status" -> "KO", "error" -> "")
    }
    case c:CreateOffer =>
      val oldSender = sender()
      val userId = c.userId

      (system.actorSelection(s"/user/market_$marketId/$userId") ? c).mapTo[Any].map { message =>
        println("YEAAAAAH " + message.toString)
        oldSender ! (message match {
          case v:OfferCreationOK=>
            Json.obj("status" ->"OK", "content" -> Json.toJson(v.offer))
          case v:OfferCreationError =>
            Json.obj("status" ->"KO","error" ->v.error)
        })
      }



  }

}