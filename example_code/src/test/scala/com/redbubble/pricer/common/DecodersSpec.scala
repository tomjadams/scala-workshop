package com.redbubble.pricer.common

import com.redbubble.pricer.common.Decoders.{cartDecoder, cartItemDecoder}
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{EitherValues, FlatSpec, Matchers}

final class DecodersSpec extends FlatSpec with Matchers with PropertyChecks with Generators with EitherValues {
  private val genCartOptions = Gen.mapOf(genStringTuple)

  private val cartItemGenerator = for {
    productType <- Gen.alphaLowerStr
    options <- genCartOptions
    markup <- Gen.posNum[Int]
    quantity <- Gen.posNum[Int]
  } yield CartItem(productType, options, markup, quantity)

  private val cartGenerator = for {
    item <- nonEmptyListOfN(5, cartItemGenerator)
  } yield Cart(item)

  "A cart item" should "be decoded from their JSON representation" in {
    forAll(cartItemGenerator) { (item: CartItem) =>
      val json = cartItemJson(item)
      val decodedItem = JsonOps.decodeJson(json)(cartItemDecoder)
      decodedItem.right.value shouldEqual item
    }
  }

  "A cart" should "be decoded from its JSON representation" in {
    forAll(cartGenerator) { (cart: Cart) =>
      val json = cartJson(cart)
      val decodedCart = JsonOps.decodeJson(json)(cartDecoder)
      decodedCart.right.value shouldEqual cart
    }
  }

  private def cartJson(cart: Cart): String = cart.items.map(cartItemJson).mkString("[", ",", "]")

  private def cartItemJson(cartItem: CartItem): String =
    s"""
       |{
       |  "product-type": "${cartItem.productType}",
       |  "options": ${cartItemOptionsJson(cartItem.options)},
       |  "artist-markup": ${cartItem.artistMarkup},
       |  "quantity": ${cartItem.quantity}
       |}
     """.stripMargin

  private def cartItemOptionsJson(options: Map[String, String]): String = {
    val kvsJson = options.map {
      case (k, v) => s""" "$k": "$v" """.trim
    }
    kvsJson.mkString("{", ",", "}")
  }
}
