package com.redbubble.pricer.common

import com.redbubble.pricer.common.Decoders.cartItemDecoder
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{EitherValues, FlatSpec, Matchers}

final class EncodersSpec extends FlatSpec with Matchers with PropertyChecks with Generators with EitherValues {
  private val genCartOptions = Gen.mapOf(genNestedStringTuple)

  private val baseProductGenerator = for {
    productType <- Gen.alphaLowerStr
    options <- genCartOptions
    basePrice <- Gen.posNum[Int]
  } yield BaseProduct(productType, options, basePrice)

  "A base product" should "be encoded to its JSON representation" in {
    forAll(baseProductGenerator) { (product: BaseProduct) =>
      val json = cartItemJson(product)
      val decodedItem = JsonOps.decodeJson(json)(cartItemDecoder)
      decodedItem.right.value shouldEqual product
    }
  }

  private def baseProductJson(product: BaseProduct) =
    s"""
       |{
       |  "product-type": "${product.productType}",
       |  "options": ${cartItemOptionsJson(product.options)},
       |  "base-price": ${product.basePrice}
       |}
     """.stripMargin

  private def cartItemOptionsJson(options: Map[String, Seq[String]]): String = {
    val kvsJson = options.map {
      case (k, v) => s""" "$k": "${v.mkString("[", ",", "]")}" """.trim
    }
    kvsJson.mkString("{", ",", "}")
  }
}
