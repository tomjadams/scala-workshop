package com.redbubble.pricer

import io.circe.Decoder

object Decoders {
  val cartItemDecoder = Decoder.instance[CartItem] { c =>
    for {
      productType <- c.downField("product-type").as[String]
    } yield CartItem(productType, Map.empty, 0, 0)
  }

  //  val cartDecoder = Decoder.instance[Cart] { (c: HCursor) =>
  //  }
}
