package com.redbubble.pricer.common

import com.twitter.io.Buf
import io.circe.{Decoder, Encoder, Error, Json, ParsingFailure}

trait JsonOps {
  final def encode[A](a: A)(implicit encoder: Encoder[A]): Json = encoder(a)

  final def decode[A](input: String)(implicit decoder: Decoder[A]): Either[Error, A] =
    parseJson(input).flatMap(decoder.decodeJson)

  final def decode[A](input: Buf)(implicit decoder: Decoder[A]): Either[Error, A] =
    parseJson(input).flatMap(decoder.decodeJson)

  final def parseJson(input: String): Either[ParsingFailure, Json] = io.circe.jawn.parse(input)

  final def parseJson(input: Buf): Either[ParsingFailure, Json] =
    io.circe.jawn.parseByteBuffer(BufOps.bufToByteBuffer(input))
}

object JsonOps extends JsonOps
