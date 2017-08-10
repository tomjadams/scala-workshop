# Getting Started

OK, now's the time where we are going to do some actual work. We're going to build a simplified version of the [pricing calculator homework task](http://take-home-test.herokuapp.com/new-product-engineer).

Feel free to take a read over the task itself, we're going to start off with the basic idea of the task (read some JSON data from the command line), but we'll be simplifying it by:

* Not worrying about all the edge cases; missing fields, cart with unknown product, etc.
* Assuming that the structure of the JSON file is stable (e.g. order of keys);

Go ahead & download all the JSON data files linked off the homework exercise, you won't need the schema files (but they may help you understand the structure/types). You'll want to put these into one of the `resources` directories, I'd recommend:

* Put the `base-prices.json` file into `src/main/resources` (this is a core part of the system);
* Put the `cart-*.json` files into `src/test/resources` (we'll use these for testing).

## Parsing command line arguments

Like most languages with a C heritage, Scala (by virtue of being a descendant of Java), takes a pretty typical approach of having a single class which is the entry-point to the app, containing a `main` function which takes an array of command line arguments.

The example that has been generated for you simplifies this, by extending the `App` trait, but you probably want to build this yourself, such that you can do some more complex things, like check we have the right number of arguments. What you want to do here is rename the current main class, feel free to move it's package to something more relevant, e.g. `com.redbubble.pricing` or such.

```scala

```



**Further reading:**

You've been introduced to a few new concepts, now's probably a good time to read up on them.

* `trait`;
* `object`;
* Package structure;
* Pattern matching.




TODo Explain what to do.

http://www.scala-sbt.org/0.13/docs/Library-Dependencies.html#Managed+Dependencies


http://www.scalatest.org