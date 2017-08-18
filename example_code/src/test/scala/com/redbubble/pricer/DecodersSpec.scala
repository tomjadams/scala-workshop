package com.redbubble.pricer

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

final class DecodersSpec extends FlatSpec with Matchers with PropertyChecks {

  val genCartOptions: Gen[Map[String, String]] = Gen.mapOf(genStringTuple)

  private val cartItemGenerator = for {
    productType <- Gen.alphaLowerStr
    options <- Gen.mapOfN(5)
  } yield CartItem(productType, Map.empty, 0, 0)

  "A cart item" should "" in {
    forAll { (productType: String) =>
      val product = CartItem(productType, Map.empty, 0, 0)
      product.productType shouldEqual productType
      product.options shouldEqual options
      product.basePrice shouldEqual basePrice
    }
  }

}
