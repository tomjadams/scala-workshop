package com.redbubble.pricer.http

import cats.data.NonEmptyList
import com.redbubble.pricer.common.Encoders.baseProductsEncoder
import com.redbubble.pricer.common._
import com.twitter.finagle.Http
import com.twitter.io.Buf
import com.twitter.util.{Await, Return, Throw, Try}
import io.circe.Encoder.encodeInt
import io.circe.{Decoder, Encoder, Json, Printer}
import io.finch.{Endpoint, _}

trait ResponseEncoders {
  implicit val productsResponseEncode: Encode.Json[NonEmptyList[BaseProduct]] = dataJsonEncode(baseProductsEncoder)
  implicit val priceResponseEncode: Encode.Json[Int] = dataJsonEncode(encodeInt)

  private val printer = Printer.noSpaces.copy(dropNullKeys = true)

  final def dataJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, charset) =>
      val json = Json.obj("data" -> encoder.apply(a))
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer(json, charset))
    }
}

trait RequestDecoders {
  implicit val cartRequestDecode: Decode.Json[Cart] = decodeDataJson(Decoders.cartDecoder)

  final def decodeDataJson[A](implicit decoder: Decoder[A]): Decode.Json[A] =
    Decode.json { (payload, _) =>
      val requestDecoder = dataFieldObjectDecoder(decoder)
      decodePayload(payload, requestDecoder)
    }

  private def dataFieldObjectDecoder[A](implicit decoder: Decoder[A]): Decoder[A] =
    Decoder.instance(c => c.downField("data").as[A](decoder))

  // Our Decode.Json instance takes expects a Try, but we have an Either, so we need to convert it
  private def decodePayload[A](payload: Buf, decoder: Decoder[A]): Try[A] =
    JsonOps.decode(payload)(decoder) match {
      case Left(error) => Throw(new Exception(s"Unable to decode JSON payload: ${error.getMessage}", error))
      case Right(value) => Return(value)
    }
}

trait Codecs extends RequestDecoders with ResponseEncoders

abstract class Endpoints(pricer: Pricer) extends Codecs {
  val api = "v1" :: (prices :+: price)

  private def prices: Endpoint[NonEmptyList[BaseProduct]] = get("prices") {
    Ok(pricer.baseProducts)
  }

  private def price: Endpoint[Int] = post("price" :: jsonBody[Cart]) { (cart: Cart) =>
    Ok(pricer.priceFor(cart))
  }
}

final class PricingServer(pricer: Pricer) extends Endpoints(pricer) {
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
