# Getting Started

OK, now's the time where we are going to do some actual work. We're going to build a simplified version of the [pricing calculator homework task](http://take-home-test.herokuapp.com/new-product-engineer).

Feel free to take a read over the task itself, we're going to start off with the basic idea of the task (read some JSON data from the command line), but we'll be simplifying it by:

* Not worrying about all the edge cases; missing fields, cart with unknown product, etc.
* Assuming that the structure of the JSON file is stable (e.g. order of keys);
* Not worrying about the constant time lookup.

Go ahead & download all the JSON data files linked off the homework exercise, you won't need the schema files (but they may help you understand the structure/types). You'll want to put these into one of the `resources` directories, I'd recommend:

* Put the `base-prices.json` file into `src/main/resources` (this is a core part of the system);
* Put the `cart-*.json` files into `src/test/resources` (we'll use these for testing).

## Parsing command line arguments

Like most languages with a C heritage, Scala (by virtue of being a descendant of Java), takes a pretty typical approach of having a single class which is the entry-point to the app, containing a `main` function which takes an array of command line arguments.

The example that has been generated for you simplifies this, by extending the `App` trait, but you probably want to build this yourself, such that you can do some more complex things, like check we have the right number of arguments. What you want to do here is rename the current main class, feel free to move it's package to something more relevant, e.g. `com.redbubble.pricer` or such. If you move the production code, also move the test.

A simple main class looks something like this:

```scala
object App {
  def main(args: Array[String]): Unit = {
    ...
  }
}
```

What you want to do next is to parse the command line arguments, you can use an `if` for this, or you can use pattern matching:

```scala
package com.redbubble.pricer

object App {
  def main(args: Array[String]): Unit =
    args match {
      case Array(cartFile, basePricesFile) => println(s"Cart file: ${cartFile}, base price file: ${basePricesFile}")
      case _ => println("Usage: pricer.App <cart.json> <base-prices.json>")
    }
}
```

Go ahead & run the app now to see what it outputs:

```
> run
[info] Running com.redbubble.pricer.App 
Usage: pricer.App <cart.json> <base-prices.json>
[success] Total time: 0 s, completed 10/08/2017 1:16:38 PM
> 
```

And let's give it some files:

```
> run /Users/tom/Projects/Personal/scala-workshop/example_code/src/test/resources/cart-0.json /Users/tom/Projects/Personal/scala-workshop/example_code/src/main/resource/base-prices.json
[info] Running com.redbubble.pricer.App /Users/tom/Projects/Personal/scala-workshop/example_code/src/test/resources/cart-0.json /Users/tom/Projects/Personal/scala-workshop/example_code/src/main/resource/base-prices.json
Cart file: /Users/tom/Projects/Personal/scala-workshop/example_code/src/test/resources/cart-0.json, base price file: /Users/tom/Projects/Personal/scala-workshop/example_code/src/main/resource/base-prices.json
[success] Total time: 0 s, completed 10/08/2017 1:18:19 PM
>
```

You can also run this from the command line directly as such:

```
$ ./sbt "run /Users/tom/Projects/Personal/scala-workshop/example_code/src/test/resources/cart-0.json /Users/tom/Projects/Personal/scala-workshop/example_code/src/main/resource/base-prices.json"
```

**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* `trait`;
* `object`;
* Printing to standard out;
* Package structure & the relationship to directories;
* Pattern matching.
