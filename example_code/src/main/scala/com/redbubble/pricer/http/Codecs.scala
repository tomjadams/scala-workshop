package com.redbubble.pricer.http

import com.redbubble.pricer.common.Encoders.baseProductsEncoder
import com.redbubble.pricer.common._
import com.twitter.io.Buf
import com.twitter.util.{Return, Throw, Try}
import io.circe.Encoder.encodeInt
import io.circe.{Decoder, Encoder, Json, Printer}
import io.finch._

trait ResponseEncoders {
  implicit val productsResponseEncode = dataJsonEncode(baseProductsEncoder)
  implicit val priceResponseEncode = dataJsonEncode(encodeInt)

  private val printer = Printer.noSpaces.copy(dropNullKeys = true)

  final def dataJsonEncode[A](implicit encoder: Encoder[A]): Encode.Json[A] =
    Encode.json { (a, charset) =>
      val json = Json.obj("data" -> encoder.apply(a))
      Buf.ByteBuffer.Owned(printer.prettyByteBuffer(json, charset))
    }
}

trait RequestDecoders {
  implicit val cartRequestDecode = decodeDataJson(Decoders.cartDecoder)

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
