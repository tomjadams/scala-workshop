# Finch

## Setup

Next, we're going to expose our pricing engine via a HTTP REST-ful endpoint. To do this, we're going to use [Finch](http://finagle.github.io/finch/), which is a small library built on top of [Finagle](https://twitter.github.io/finagle/).

If at any point you get into trouble, there's some examples & documentation that might help.

* [Finch Documentation](http://finagle.github.io/finch/)
* [Cookbook](http://finagle.github.io/finch/cookbook.html) - Some commone how-tos;
* [Best Practices](http://finagle.github.io/finch/best-practices.html) - Best practices for building Finch services;

The first thing that we're going to do is bring Finch into our dependencies:

```scala
val finchVersion = "0.15.1"
lazy val finchCore = "com.github.finagle" %% "finch-core" % finchVersion
lazy val finchCirce = "com.github.finagle" %% "finch-circe" % finchVersion

...

libraryDependencies ++= Seq(
  circeCore,
  circeGeneric,
  circeParser,
  finchCore,
  finchCirce,
  scalaTest,
  scalaCheck
)
```

I'd also strongly recommend that you install [sbt-revolver](https://github.com/spray/sbt-revolver), this is an sbt plugin that will watch for code changes & restart your server when it does.

Add the following dependency to your `project/plugins.sbt`:

```scala
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.0")
```

And restart your sbt REPL.

## A Simple Server

Let's start by exposing our base prices over a HTTP `GET`. Then we'll expose a `POST` endpoint to post carts to, where we'll make use of the decoders that wrote earlier.

The first thing we need to do is to create another main class to run our HTTP server.






```shell
$ ./sbt ~re-start
```

