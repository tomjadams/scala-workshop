package com.redbubble.pricer

import org.scalatest.{FlatSpec, Matchers}

final class BaseProductSpec extends FlatSpec with Matchers {
  "A base product" should "contain a product type" in {
    BaseProduct("hoodie", Map.empty, 0).productType shouldEqual "hoodie"
  }

  "A base product" should "contain options" in {
    BaseProduct("hoodie", Map("k" -> Seq("v1", "v2")), 0).options shouldEqual Map("k" -> Seq("v1", "v2"))
  }

  "A base product" should "have a base price" in {
    BaseProduct("hoodie", Map.empty, 10).basePrice shouldEqual 10
  }
}
