# Summary

OK, let's see where we are. By now, you should have:

* Gotten a basic Scala setup up & running;
* Written some basic & more advanced tests using ScalaTest & ScalaCheck;
* Parsed the cart & base product input files.

We're going to build upon the work you've done so far, so now is a good time to complete the remainder of the pricing calculator. At the very least, you should build a class that takes a set of base prices and a cart, and calculates a total price:

```scala
package com.redbubble

import cats.data.NonEmptyList
import com.redbubble.pricer.{BaseProduct, Cart}

final class Pricer(basePrices: NonEmptyList[BaseProduct]) {
  def priceFor(cart: Cart): Int = {
    0
  }
}
```

Note that we've introduce the type `NonEmptyList`; this is a list that is guarenteed (by the compiler) to not be empty (i.e. you cannot construct an empty list). This is an important concept in Scala, where possible you want to make use of the type system (the compiler) to ensure that you cannot construct objects in invalid states. This has a nice added benefit that you don't need to write as many tests, and the feedback loop from the compiler is faster (than tests).
