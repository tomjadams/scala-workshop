package com.redbubble.pricer.common

import com.redbubble.pricer.common.Encoders.baseProductEncoder
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
      val expected = JsonOps.parseJson(baseProductJson(product))
      val actual = JsonOps.parseJson(JsonOps.encode(product)(baseProductEncoder).noSpaces)
      expected shouldEqual actual
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

  private def cartItemOptionsJson(options: Map[String, Seq[String]]) = {
    val kvsJson = options.map {
      case (k, v) => s""" "$k": ${v.map(valueJson).mkString("[", ",", "]")} """.trim
    }
    kvsJson.mkString("{", ",", "}")
  }

  private def valueJson(v: String) = s""" "$v" """.trim
}
