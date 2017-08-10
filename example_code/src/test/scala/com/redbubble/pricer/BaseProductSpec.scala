package com.redbubble.pricer

import org.scalatest.{FlatSpec, Matchers}

final class BaseProductSpec extends FlatSpec with Matchers {
  "A base product" should "contain a product type " in {
    BaseProduct("hoodie", Map.empty, 0).productType shouldEqual "hoodie"
  }
}
