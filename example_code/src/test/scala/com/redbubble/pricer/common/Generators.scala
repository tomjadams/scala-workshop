package com.redbubble.pricer.common

import org.scalacheck.Gen

trait Generators {
  final val genStringValue: Gen[String] = Gen.alphaStr.map(_.take(20))

  final val genNotEmptyString: Gen[String] = Gen.alphaStr.suchThat(s => !s.isEmpty)

  final val genStringTuple: Gen[(String, String)] = for {
    k <- Gen.identifier
    v <- genStringValue
  } yield (k, v)

  final val genNestedStringTuple: Gen[(String, Seq[String])] = for {
    k <- Gen.identifier
    v <- Gen.oneOf(Gen.const(Seq.empty), Gen.listOfN(5, genStringValue))
  } yield (k, v)

  final def nonEmptyListOfN[A](n: Int, gen: Gen[A]): Gen[Seq[A]] =
    Gen.listOfN(n, gen).suchThat(as => as.nonEmpty)
}

object Generators extends Generators
