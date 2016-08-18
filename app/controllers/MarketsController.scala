package controllers

import javax.inject._


import actors.MarketActor
import actors.messages.MessagesActor.TakeOffer
import akka.actor.ActorSystem
import models.daos._
import models.entities.{User, Market, Supplier}
import models.persistence.SlickTables.SuppliersTable
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MarketsController @Inject()(userDAO: UserDAO,marketDAO: MarketDAO,offerDAO: OfferDAO,productDAO: ProductDAO, productTypeDAO: ProductTypeDAO, transactionDAO: TransactionDAO,multipleDAO: MultipleDAO)(implicit ec: ExecutionContext, system: ActorSystem, mat: akka.stream.Materializer) extends Controller {

  import akka.pattern.ask
  import scala.concurrent.duration._
  import akka.util.Timeout
  implicit val timeout: Timeout = 10.seconds
  var markets = Seq[Market]()
  for( n <- 1 until 5){
    markets.:+(Market(0,n.toString,s"market $n"))

  }
  val initGetMarkets =Action.async{
    marketDAO.all  map { re => re.foreach(m => initiateMarketActor(m.id))


      Ok( "Markets initiated")}

  }

  def initiateMarketActor(id: Long) = {
    val name =s"market_$id"
    system.actorOf(MarketActor.props(id,name,userDAO,offerDAO,productDAO,transactionDAO,multipleDAO), name)
  }
  def takeOffer(marketId: Long, userId: Long, offerId : Long)=Action.async{
    (system.actorSelection(s"/user/market_$marketId") ? TakeOffer(marketId,userId,offerId)).mapTo[String].map { message =>
      Ok(message)
    }

  }

  def addMarket(market: Market)=Action.async{
      //val newMarket = Market(0,name,desc)
      marketDAO.insert(market)  map { re =>

        initiateMarketActor(re)
        Ok( re.toString())}

  }
  def addTestUser()=Action.async{
    userDAO.insert(User(0,"a","a","a","a",1))  map { re => Ok( re.toString())}
  }
  def addMarket(name:String,desc: String):Action[AnyContent]=
    addMarket(Market(0,name,desc))


  def createTestMarkets()= Action.async(marketDAO.insert(markets)  map { re => Ok( re.toString())})

  def getMarkets() = Action.async{
    marketDAO.all map { re => Ok(re.toString())}
  }

  def getTransactions() = Action.async{
    transactionDAO.all map { re => Ok(re.toString())}
  }



}
