package com.redbubble.pricer

import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

final class DecodersSpec extends FlatSpec with Matchers with PropertyChecks with Generators {
  private val genCartOptions = Gen.mapOf(genStringTuple)

  private val cartItemGenerator = for {
    productType <- Gen.alphaLowerStr
    options <- genCartOptions
    markup <- Gen.posNum[Int]
    quantity <- Gen.posNum[Int]
  } yield CartItem(productType, options, markup, quantity)

  "A cart item" should "can be decoded from their JSON representation" in {
    forAll(cartItemGenerator) { (item: CartItem) =>


    }
  }
}
