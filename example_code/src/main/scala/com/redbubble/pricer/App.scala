package com.redbubble.pricer

import java.io.File

import com.redbubble.Pricer
import com.redbubble.pricer.Decoders.cartDecoder
import io.circe.Decoder

import scala.io.Source

object App {
  def main(args: Array[String]): Unit = args match {
    case Array(cartFile: String, basePricesFile: String) =>
      val priceResult = computePrice(cartFile, basePricesFile)
      priceResult match {
        case Right(price) => println(price)
        case Left(error) => sys.error(s"Unable to calculate cart price: ${error.getMessage}")
      }
    case _ => println("Usage: pricer.App <cart.json> <base-prices.json>")
  }

  private def computePrice(cartFilename: String, basePricesFilename: String) =
    for {
      cart <- decodeJsonFile(cartFilename, cartDecoder)
      price <- new Pricer().priceFor(cart)
    } yield price

  private def decodeJsonFile[A](filename: String, decoder: Decoder[A]) = {
    val contents = readJson(filename)
    JsonOps.decodeJson(contents)(decoder)
  }

  private def readJson(filename: String) = Source.fromFile(new File(filename)).mkString
}
