package com.redbubble.pricer.common

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

final class BaseProductPropertySpec extends FlatSpec with Matchers with PropertyChecks {
  "A base product" should "contain a product type" in {
    forAll { (productType: String, options: Map[String, Seq[String]], basePrice: Int) =>
      val product = BaseProduct(productType, options, basePrice)
      product.productType shouldEqual productType
      product.options shouldEqual options
      product.basePrice shouldEqual basePrice
    }
  }
}
