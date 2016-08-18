package actors

import actors.messages.MessagesActor.TakeOffer
import akka.actor._
import models.daos._
import models.entities._
object MarketActor{
  def props = Props[MarketActor]
  def props(marketId: Long,name: String,userDAO: UserDAO,offerDAO: OfferDAO,productDAO: ProductDAO,transactionDAO: TransactionDAO,multipleDAO: MultipleDAO) =
    Props(classOf[MarketActor],marketId,name,userDAO,offerDAO,productDAO,transactionDAO,multipleDAO)
}
class MarketActor(marketId: Long,name: String,userDAO: UserDAO,offerDAO:OfferDAO,productDAO:ProductDAO,transactionDAO:TransactionDAO,multipleDAO: MultipleDAO) extends Actor{

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
  def receive = {
    case t:TakeOffer => {
      val oldSender = sender
      val userid = t.userId
      println("envio el mensaje")
      (system.actorSelection(s"/user/market_$marketId/$userid") ? t).mapTo[String].map { message =>
        println(message)
        oldSender ! message
      }

    }

  }

}