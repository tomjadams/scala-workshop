package com.redbubble.pricer.shell

import java.io.File

import cats.data.NonEmptyList
import com.redbubble.pricer.common.Decoders.cartDecoder
import com.redbubble.pricer.common.{BaseProduct, JsonOps, Pricer}
import io.circe.Decoder

import scala.io.Source

object CommandLineApp {
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
      price = new Pricer(NonEmptyList.of(BaseProduct("hoodie", Map.empty, 0))).priceFor(cart)
    } yield price

  private def decodeJsonFile[A](filename: String, decoder: Decoder[A]) = {
    val contents = readJson(filename)
    JsonOps.decodeJson(contents)(decoder)
  }

  private def readJson(filename: String) = Source.fromFile(new File(filename)).mkString
}
