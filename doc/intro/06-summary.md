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
