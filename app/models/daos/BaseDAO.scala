package models.daos

import javax.inject.{Inject, Singleton}

import models.entities._
import models.persistence.SlickTables
import models.persistence.SlickTables.BaseTable
import play.api.Play
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.lifted.{CanBeQueryCondition}
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

trait AbstractBaseDAO[T,A] {
  def insert(row : A): Future[Long]
  def insert(rows : Seq[A]): Future[Seq[Long]]
  def update(row : A): Future[Int]
  def update(rows : Seq[A]): Future[Unit]
  def findById(id : Long): Future[Option[A]]
  def findByFilter[C : CanBeQueryCondition](f: (T) => C): Future[Seq[A]]
  def deleteById(id : Long): Future[Int]
  def deleteById(ids : Seq[Long]): Future[Int]
  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int]
}


abstract class  BaseDAO[T <: BaseTable[A], A <: BaseEntity]() extends AbstractBaseDAO[T,A] with HasDatabaseConfig[JdbcProfile] {
  protected lazy val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  protected val tableQ: TableQuery[T]

  def insert(row : A): Future[Long] ={
    insert(Seq(row)).map(_.head)
  }

  def insert(rows : Seq[A]): Future[Seq[Long]] ={
    db.run(tableQ returning tableQ.map(_.id) ++= rows.filter(_.isValid))
  }

  def update(row : A): Future[Int] = {
    if (row.isValid)
      db.run(tableQ.filter(_.id === row.id).update(row))
    else
      Future{0}
  }

  def update(rows : Seq[A]): Future[Unit] = {
    db.run(DBIO.seq((rows.filter(_.isValid).map(r => tableQ.filter(_.id === r.id).update(r))): _*))
  }

  def findById(id : Long): Future[Option[A]] = {
    db.run(tableQ.filter(_.id === id).result.headOption)
  }

  def findByFilter[C : CanBeQueryCondition](f: (T) => C): Future[Seq[A]] = {
    db.run(tableQ.withFilter(f).result)
  }

  def deleteById(id : Long): Future[Int] = {
    deleteById(Seq(id))
  }

  def deleteById(ids : Seq[Long]): Future[Int] = {
    db.run(tableQ.filter(_.id.inSet(ids)).delete)
  }

  def deleteByFilter[C : CanBeQueryCondition](f:  (T) => C): Future[Int] = {
    db.run(tableQ.withFilter(f).delete)
  }

}
  @Singleton
  class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    import dbConfig.driver.api._
    import dbConfig.db

    protected val tableQ = SlickTables.usersTableQ

    def all: Future[Seq[User]] = {
      db.run(tableQ.result)
    }

    def insert(obj: User): Future[Long] = {
      db.run(tableQ returning tableQ.map(_.id) += obj)
    }


    def byId(id: Long): Future[Option[User]] = {
      db.run(tableQ.filter(_.id === id).result.headOption)
    }
    def byMarket(marketId: Long): Future[Seq[User]] = {
      db.run(tableQ.filter(_.marketId === marketId).result)
    }
  }



  @Singleton
  class MarketDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    import dbConfig.driver.api._
    import dbConfig.db

    protected val tableQ = SlickTables.marketsTableQ

    def all: Future[Seq[Market]] = {
      db.run(tableQ.result)
    }

    def insert(obj: Market): Future[Long] = {
      db.run(tableQ returning tableQ.map(_.id) += obj)
    }


    def byId(id: Long): Future[Option[Market]] = {
      db.run(tableQ.filter(_.id === id).result.headOption)
    }
    def insert(rows : Seq[Market]): Future[Seq[Long]] ={
      db.run(tableQ returning tableQ.map(_.id) ++= rows.filter(_.isValid))
    }

  }

  @Singleton
  class OfferDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    import dbConfig.driver.api._
    import dbConfig.db

    protected val tableQ = SlickTables.offersTableQ

    def all: Future[Seq[Offer]] = {
      db.run(tableQ.result)
    }

    def insert(obj: Offer): Future[Long] = {
      db.run(tableQ returning tableQ.map(_.id) += obj)
    }


    def byId(id: Long): Future[Option[Offer]] = {
      db.run(tableQ.filter(_.id === id).result.headOption)
    }
    def byMarket(marketId: Long): Future[Seq[Offer]] = {
      db.run(tableQ.filter(_.marketId === marketId).result)
    }

  }
  @Singleton
  class ProductDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    import dbConfig.driver.api._
    import dbConfig.db

    protected val tableQ = SlickTables.productsTableQ

    def all: Future[Seq[Product]] = {
      db.run(tableQ.result)
    }

    def insert(obj: Product): Future[Long] = {
      db.run(tableQ returning tableQ.map(_.id) += obj)
    }


    def byId(id: Long): Future[Option[Product]] = {
      db.run(tableQ.filter(_.id === id).result.headOption)
    }
    def byUser(userId: Long): Future[Seq[Product]] = {
      db.run(tableQ.filter(_.userId === userId).result)
    }
    def byUserIdAndProductTypeId(userId: Long, pTypeId: Long): Future[Option[Product]] = {
      db.run(tableQ.filter(x=>{
        x.userId === userId}).filter(_.productTypeId === pTypeId).result.headOption)
    }

  }

  @Singleton
  class ProductTypeDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
    val dbConfig = dbConfigProvider.get[JdbcProfile]

    import dbConfig.driver.api._
    import dbConfig.db

    protected val tableQ = SlickTables.productTypesTableQ

    def all: Future[Seq[ProductType]] = {
      db.run(tableQ.result)
    }

    def insert(obj: ProductType): Future[Long] = {
      db.run(tableQ returning tableQ.map(_.id) += obj)
    }


    def byId(id: Long): Future[Option[ProductType]] = {
      db.run(tableQ.filter(_.id === id).result.headOption)
    }
  }

@Singleton
class TransactionDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._
  import dbConfig.db

  protected val tableQ = SlickTables.transactionsTableQ

  def all: Future[Seq[Transaction]] = {
    db.run(tableQ.result)
  }

  def insert(obj: Transaction): Future[Long] = {
    db.run(tableQ returning tableQ.map(_.id) += obj)
  }


  def byId(id: Long): Future[Option[Transaction]] = {
    db.run(tableQ.filter(_.id === id).result.headOption)
  }
}

@Singleton
class MultipleDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._
  import dbConfig.db

  protected val tableMarket = SlickTables.marketsTableQ
  protected val tableOffer = SlickTables.offersTableQ
  protected val tableTransaction = SlickTables.transactionsTableQ
  protected val tableProductType = SlickTables.productTypesTableQ
  protected val tableUser = SlickTables.usersTableQ
  protected val tableProduct = SlickTables.productsTableQ
  lazy val schema = tableMarket.schema  ++ tableProductType.schema ++ tableUser.schema++ tableProduct.schema++tableOffer.schema++ tableTransaction.schema
  def holi() ={
    db.run(DBIO.seq(schema.create))/*,tableUser returning tableUser.map(_.id) += User(0, "holi1", "holo", "hola", "he", 0, null, null),
   tableUser returning tableUser.map(_.id) += User(0, "holi2", "holo", "hola", "he", 0, null, null),
    tableMarket returning tableMarket.map(_.id) += Market(0, "test1", "test", null, null),
   tableMarket returning tableMarket.map(_.id) += Market(0, "test2", "test", null, null),
    tableProductType returning tableProductType.map(_.id) += ProductType(0, "test", null, null),
    tableProduct returning tableProduct.map(_.id) += Product(0, 1, 1, 200, 1, 1, null, null),
    tableOffer returning tableOffer.map(_.id) += Offer(0, 1, 1, 200, 1, 2, 200, null, null))*/

  }
  def holu()= {
    db.run(for {
        p1 <- tableUser returning tableUser.map(_.id) += User(0, "holi1", "holo", "hola", "he", 0)
        p2 <- tableUser returning tableUser.map(_.id) += User(0, "holi2", "holo", "hola", "he", 0)
        p3 <- tableMarket returning tableMarket.map(_.id) += Market(0, "test1", "test")
        p4 <- tableMarket returning tableMarket.map(_.id) += Market(0, "test2", "test")
        p5 <- tableProductType returning tableProductType.map(_.id) += ProductType(0, "test")
        p6 <- tableProduct returning tableProduct.map(_.id) += Product(0, 1, 1, 200, 1, 1)
        p7 <- tableOffer returning tableOffer.map(_.id) += Offer(0, 1, 1, 200, 1, 2, 200)
        p8 <- tableOffer.result
      } yield p8)
  }
  def test3()={
    db.run(tableUser returning tableUser.map(_.id) += User(0, "holi1", "holo", "hola", "he", 0))
  }
  def run() ={
    schema.create.statements.toString()

  }
  def drop ()={
    schema.drop.statements.toString()
  }



  def completeTransaction(product: Product, product2: Product,product3: Product,product4: Product, transaction: Transaction, offer: Offer): Future[Unit] = {
    println(s"product:$product product2:$product2")
    val dbAction = (
      for {
        productA <- {tableProduct.filter(_.id === product.id)
          .map(x => (x.productQuantity))
          .update(product.productQuantity)}
        productB <- {tableProduct.filter(_.id === product2.id)
          .map(x => (x.productQuantity))
          .update(product2.productQuantity)}
        productC <- {tableProduct.filter(_.id === product3.id)
          .map(x => (x.productQuantity))
          .update(product3.productQuantity)}
        productD <- {tableProduct.filter(_.id === product4.id)
          .map(x => (x.productQuantity))
          .update(product4.productQuantity)}
        offer <- tableOffer.filter(_.wantedUserId=== product2.userId).delete
        offer3 <- tableOffer.filter(_.wantedUserId=== product.userId).delete
        transaction <- tableTransaction returning tableTransaction.map(_.id) += transaction
      } yield ()
      ).transactionally

    db.run(dbAction)
  }
}




