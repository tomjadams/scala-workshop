package com.redbubble.pricer.http

import cats.data.NonEmptyList
import com.redbubble.pricer.common.Encoders.baseProductsEncoder
import com.redbubble.pricer.common.{BaseProduct, Cart, Pricer}
import com.twitter.finagle.Http
import com.twitter.io.Buf
import com.twitter.util.Await
import io.circe.{Encoder, Json, Printer}
import io.finch.{Endpoint, _}

trait ResponseEncoders {
  implicit val productsResponseEncode: Encode.Json[NonEmptyList[BaseProduct]] = dataJsonEncode(baseProductsEncoder)

  private val printer = Printer.noSpaces.copy(dropNullKeys = true)

  final def dataJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, charset) =>
      val json = Json.obj("data" -> encoder.apply(a))
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer(json, charset))
    }
}

trait RequestDecoders {
  // decoders go here
}

trait Codecs extends RequestDecoders with ResponseEncoders

abstract class Endpoints(pricer: Pricer) {
  private val prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  private val price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
    Ok(1)
  }

  val api = "v1" :: (prices :+: price)
}

final class PricingServer(pricer: Pricer) extends Endpoints(pricer) with Codecs {
  def start(): Unit = {
    val server = Http.server.serve(":8081", api.toService)
    Await.ready(server)
    ()
  }
}

object HttpApp {
  def main(args: Array[String]): Unit = {
    val pricer = new Pricer(NonEmptyList.of(BaseProduct("hoodie", Map.empty, 0)))
    new PricingServer(pricer).start()
  }
}
