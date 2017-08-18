package com.redbubble.pricer

import io.circe.Decoder

object Decoders {
  implicit val cartItemDecoder = Decoder.instance[CartItem] { c =>
    for {
      productType <- c.downField("product-type").as[String]
      options <- c.downField("options").as[Map[String, String]]
      markup <- c.downField("artist-markup").as[Int]
      quantity <- c.downField("quantity").as[Int]
    } yield CartItem(productType, options, markup, quantity)
  }

  val cartDecoder = Decoder[List[CartItem]].map(Cart)
}
