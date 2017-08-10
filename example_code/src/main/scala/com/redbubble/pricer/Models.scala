package com.redbubble.pricer

final case class BaseProduct(productType: String, options: Map[String, Seq[String]], basePrice: Int)

final case class CartItem(
    productType: String, options: Map[String, String], artistMarkup: Int, quantity: Int)

final case class Cart(items: Seq[CartItem])

