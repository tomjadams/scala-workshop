package com.redbubble.pricer

object App {
  def main(args: Array[String]): Unit =
    args match {
      case Array(cartFile, basePricesFile) => println(s"Cart file: ${cartFile}, base price file: ${basePricesFile}")
      case _ => println("Usage: pricer.App <cart.json> <base-prices.json>")
    }
}
