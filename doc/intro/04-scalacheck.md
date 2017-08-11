# ScalaCheck

## Setup

We've now seen how we can write basic tests using ScalaTest. Let's extend that a little to use [ScalaCheck](https://www.scalacheck.org), a property-based testing framework. Again, the examples are contrived, but they'll help you to learn the specifics of ScalaCheck, which you can later dig deeper into.

To bring use ScalaCheck, we're going to add the ScalaCheck library into our dependencies, which live in `Dependencies.scala`:

```scala
import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
}
```

And then add the dependency to the project:

```scala
import Dependencies._

lazy val root = (project in file(".")).
    settings(
      inThisBuild(List(
        organization := "com.redbubble",
        scalaVersion := "2.12.1",
        version := "0.1.0-SNAPSHOT"
      )),
      name := "Pricer",
      libraryDependencies ++= Seq(scalaTest, scalaCheck)
    )
```

## Simple Properties

You may have noticed when creating the tests, that you had to create a bunch of values for the fields in your class. For example, you may have a test that looks something like:

```scala
"A base product" should "contain options" in {
  BaseProduct("hoodie", Map("k" -> Seq("v1", "v2")), 0).options shouldEqual Map("k" -> Seq("v1", "v2"))
}
```

Where do these values come from? Are the values meaningful? What about edge cases? If you were really test-driving the implementation for this test, your class could simply return dummy data without actually doing anything!

```scala
final class BaseProduct(private val p: String, o: Map[String, Seq[String]], b: Int) {
  val productType = "hoodie"
  val options = Map("k" -> Seq("v1", "v2"))
  val basePrice = 0
}
```

A simple thing that you can use ScalaCheck for, is to generate random values to use in your testing. For our simple example, this means that you are forced to build the correct implementation to pass the test. This technique is known as [triangulation](https://www.google.com.au/search?q=tdd+triangulation).

Let's see what this might look like. We're going to need to bring in some of the [ScalaTest support for ScalaCheck](http://www.scalatest.org/user_guide/property_based_testing).

```scala
package com.redbubble.pricer

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

final class BaseProductPropertySpec extends FlatSpec with Matchers with PropertyChecks {
  "A base product" should "contain a product type" in {
    forAll { (productType: String) =>
      BaseProduct(productType, Map.empty, 0).productType shouldEqual productType
    }
  }
}
```

What we've now done is told ScalaCheck to generate us a bunch of random strings, and then we've made claims about the values of those. We've basically claimed that for all string values, we can pass in that string as the property type & then get it back when we ask for a product type. We've just tested a simple property getter.

We can do better than this, we can pass in all our parameters:


```scala
package com.redbubble.pricer

import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

final class BaseProductPropertySpec extends FlatSpec with Matchers with PropertyChecks {
  "A base product" should "contain a product type" in {
    forAll { (productType: String, options: Map[String, Seq[String]], basePrice: Int) =>
      val product = BaseProduct(productType, options, basePrice)
      product.productType shouldEqual productType
      product.options shouldEqual options
      product.basePrice shouldEqual basePrice
    }
  }
}
```

We can do heaps more things with ScalaCheck, we can constrain the values we generate (e.g. all product type IDs are lower case), we can ensure we test certain edge cases, etc. The [ScalaCheck documentation](https://www.scalacheck.org/documentation.html) has heaps of info on what we can do, as well as videos that may help you get started with ScalaCheck as well as use it's more advanced features.
