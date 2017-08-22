package com.redbubble.pricer.http

import cats.data.NonEmptyList
import com.redbubble.pricer.common.{BaseProduct, Cart, Pricer}
import io.finch.{Endpoint, Ok, get, jsonBody, post}

abstract class Endpoints(pricer: Pricer) extends Codecs {
  val api = "v1" :: (prices :+: price)

  private def prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  private def price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
    Ok(pricer.priceFor(cart))
  }
}
