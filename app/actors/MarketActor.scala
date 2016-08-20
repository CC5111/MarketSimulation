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
  var bienestarTotal = 0
  val initUsers ={
    userDAO.byMarket(marketId).onSuccess{
      case users:Seq[User] =>
        users.foreach( user=>  context.actorOf(UserActor.props(user.id,offerDAO,productDAO,transactionDAO,multipleDAO),user.id.toString))

      case _ =>

    }

  }


  def calculateAllBienestar()= {
    productDAO.all map{ x =>
      var sumatory = 0.0
      x.groupBy{_.id}.foreach( each =>
      sumatory+= calculateBienestar(each._2)
      )
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




  def receive = {
    case t:TakeOffer => {
      val oldSender = sender
      val userid = t.userId
      (system.actorSelection(s"/user/market_$marketId/$userid") ? t).mapTo[Any].map { message =>
        oldSender ! (message match {
          case v:TransactionSuccessfully=>
            system.actorSelection(s"/user/market_$marketId/graph") ! calculateAllBienestar()

            Json.obj("status" ->"OK"  )
          case v:TransactionError =>
            Json.obj("status" ->"KO","error" ->v.error)
          })
        }
      }
    case g:GetAllOffers =>
      val oldSender = sender()
      offerDAO.byMarket(g.marketId).onSuccess{
        case offers:Seq[Offer] => {
          oldSender ! Json.obj("status" -> "OK", "content" -> Json.toJson(offers))
        }
        case _ =>
          oldSender ! Json.obj("status" -> "KO", "error" -> "")
    }
    case c:CreateOffer =>
      val oldSender = sender()
      val userId = c.userId

      (system.actorSelection(s"/user/market_$marketId/$userId") ? c).mapTo[Any].map { message =>
        oldSender ! (message match {
          case v:OfferCreationOK=>
            Json.obj("status" ->"OK", "content" -> Json.toJson(v.offer))
          case v:OfferCreationError =>
            Json.obj("status" ->"KO","error" ->v.error)
        })
      }
    case g:GetProducts =>
      val oldSender = sender()
      val userId = g.userId
      (system.actorSelection(s"/user/market_$marketId/$userId") ? g).mapTo[Any].map { message =>
        oldSender ! (message match {
          case seq:Seq[Product] =>
            Json.obj("status" ->"OK", "content" -> Json.toJson(seq))
        })
      }

  }

}