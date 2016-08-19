package models.entities

import play.api.libs.json.{Json, Writes}

/**
 * Created by Nicolas on 18-08-2016.
 */
/*
case class Offer(id: Long, marketId: Long, offProductId: Long, offAmount:Long, wantedUserId: Long, wantedProductId: Long ,wantedAmount: Long , created: java.sql.Timestamp =DateMapper.d ) extends BaseEntity

 */
trait ImplicitsModels {
  implicit val offerWrite = new Writes[Offer] {


    def writes(abb: Offer) = Json.obj(
      // "NDB_No" ->abb.NDB_No,
      "id" ->abb.id,
      "marketId" ->abb.marketId,
      "wantsProductId" ->abb.offProductId,
      "wantsAmount" ->abb.offAmount,
      "ownerUserId" ->abb.wantedUserId,
      "givesProductId" ->abb.wantedProductId,
      "givesAmount" ->abb.wantedAmount

    )
  }
  implicit val offerSeq= new Writes[Seq[Offer]] {
    override def writes(abb: Seq[Offer]) = Json.toJson(abb.map(x=>Json.toJson(x)(offerWrite)))
  }

  implicit val productWrite = new Writes[Product] {


    def writes(abb: Product) = Json.obj(
      // "NDB_No" ->abb.NDB_No,
      "id" ->abb.id,
      "userId" ->abb.userId,
      "productTypeId" ->abb.productTypeId,
      "productConstant" ->abb.productConstant,
      "productExponential" ->abb.productExponential,
      "productQuantity" ->abb.productQuantity

    )
  }
  implicit val productSeq= new Writes[Seq[Product]] {
    override def writes(abb: Seq[Product]) = Json.toJson(abb.map(x=>Json.toJson(x)(productWrite)))
  }


}
