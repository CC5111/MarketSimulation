package utilities
import models.entities._

/**
 * Created by Nicolas on 18-08-2016.
 */
object EconomyMath {

  def partialDerivative(product: Product): Double = {
    product.productConstant*product.productExponential*Math.pow(product.productQuantity,product.productExponential-1)
  }
  def RMS(product1: Product,product2: Product): Double ={
    partialDerivative(product1)/partialDerivative(product2)
  }

}
