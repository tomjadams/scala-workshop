package com.redbubble.pricer.common

import io.circe.{Decoder, Encoder, Error, Json, ParsingFailure}

trait JsonOps {
  final def encode[A](a: A)(implicit encoder: Encoder[A]): Json = encoder(a)

  final def decodeJson[A](input: String)(implicit decoder: Decoder[A]): Either[Error, A] =
    parseJson(input).flatMap(decoder.decodeJson)

  final def parseJson(input: String): Either[ParsingFailure, Json] = io.circe.jawn.parse(input)
}

object JsonOps extends JsonOps
