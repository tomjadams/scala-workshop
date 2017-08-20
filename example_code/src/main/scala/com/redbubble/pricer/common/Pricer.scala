package com.redbubble.pricer.common

import cats.data.NonEmptyList

final class Pricer(baseProducts: NonEmptyList[BaseProduct]) {
  def priceFor(cart: Cart): Int = {
    0
  }
}
