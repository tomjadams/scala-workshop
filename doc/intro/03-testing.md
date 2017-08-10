# Testing

So by now you should have a simple app, that takes in some command line arguments & that's about it. Before we get too much further, let's create some tests for our code.

In order to make this simple, let's create some small classes that respresent our domain model. We'll be using case classes for these. You can think of case classes as something akin to structs. The Scala compiler generates some of the methods of the class for you if you add the `case` qualifier at the start of the class name. Let's go ahead and define our first class, for a product from our base prices list.

```scala
final case class BaseProduct(productType: String, options: Map[String, Seq[String]], basePrice: Int)
```

There's a fair bit going on here:

* We've created a base product class, that takes a bunch of parameters in its constructor. These are automagically assigned to member variables. The values are:
  * `productType: String` - this is saying that the variable `productType` is a `String`;
  * `options: Map[String, Seq[String]]` - `options` is a `Map` (associative array, hash, etc.), with `String` keys & values that are a sequence of `String`s. This could represent for example the JSON `"colour": ["white", "dark"]`;
  * `basePrice: Int` - `basePrice` is an integer (we are using cents in the homework task).
* The `final` modifier is saying that we're not going to allow subclassing of this class (as we've not [designed this class for extension](https://programmingideaswithjake.wordpress.com/2014/12/06/designing-classes-for-extension/)).
* The `case` modifier tell the compiler to generate some useful functions for us (`toString`, `equals`, etc.).

Let's use this class to write some tests. Note that the tests we are going to write will be quite simple, and that in reality because the functions we'll be testing are compiler generated, we'd not normally test them. However, they are simple enough to show the ways that we can test. We'll be using [ScalaTest](http://www.scalatest.org) for the testing, there are alternatives, but this is quite simple.

Here's our first test:

```scala
final class BaseProductSpec extends FlatSpec with Matchers {
  "A base product" should "contain a product type " in {
    BaseProduct("hoodie", Map.empty, 0).productType shouldEqual "hoodie"
  }
}
```

Let's unpack this a bit too.

* Again we've made our class `final`.
* We've extended from a parent class called `FlatSpec`, and the also mixed in a trait called `Matchers`. These two will give us access to the functionality we need to run the test.
* We've instantiated the class as follows: `BaseProduct(...)`, normally we'd use `new BaseProduct(...)` to do this, but because the compiler generated some things for us (as it's a case class), we can omit the `new`. If you want to dig deeper, this is because we had an `apply` method generated for us, and there's magic syntactic compiler sugar that means `BaseProduct.apply(...)` is equivalent to `BaseProduct`. You'll see this exploited a lot in Scala.

Now, let's run the test:

```
> test
[info] BaseProductSpec:
[info] A base product
[info] - should contain a product type
[info] Run completed in 316 milliseconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 1, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 1 s, completed 10/08/2017 5:19:03 PM
```

Success!!!


**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* Case classes & objects;
* The `apply` method.
