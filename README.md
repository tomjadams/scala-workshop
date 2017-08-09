# Overview

The basic agenda is to complete a simplified version of the pricing calculator homework task, and then expose a simplified version as REST API, then as a GraphQL API.

I'll help get people going, answer questions, but I won't be running it as an interactive follow-along workshop, you'll have to do the work yourself. The goal is to get you to learn by doing.

There is reading material at the end of this document.

# Agenda

1. Introduction to Scala

    You'll learn about the basics of Scala, including:

    * [Setup](./setup.md)
    * [Getting Started](./getting-started.md)
    * Testing
    * ScalaCheck
    * Either & Option
    * Circe
    * Futures

    To get started, click on the links for each of the sections. It's better if you do them in order, but feel free to skip ahead if you want.

2. Finch

    Finch is the HTTP framework we use. We'll turn the pricing calculator into a HTTP API.

3. GraphQL

    We'll take the Finch API that we built, and make turn it into GraphQL API.

4. How the iOS API works

    We'll then cover the iOS API, what it does, how it works, how it's built. Things like:

    * BFF - Rationale, what it does, etc.
    * Architecture
    * Package structure
    * Metrics
    * Monitoring
    * Deployment

# Reading material

## Learning Scala

Depending on how you like to learn, you should also do some reading on Scala. There's heaps of help.

### Books

There are a bunch of beginner Scala books (google them). The FP oriented books are good if you want that slant, though you may be best picking a more beginner's book.

* https://www.manning.com/books/functional-programming-in-scala

### Courses

If courses are more your thing, there's plenty to choose from. Ideally you should pick an introductory course, don't head straight for the deep tech.

* [Twitter Scala School](https://twitter.github.io/scala_school/) - Twitter's course is a great free resource (and covers some of the tech we use)
* [Udemy](https://www.udemy.com/courses/search/?q=scala)
* [Coursera](https://www.coursera.org/specializations/scala)

## Tools/Frameworks We Use

We make use of a bunch of frameworks, here's some more information if you'd like to dig deeper into them.

### Finch

This is the HTTP framework we use. It's a small abstraction on top of Finagle.

* [Finch best practices](https://github.com/finagle/finch/blob/master/docs/best-practices.md)
* [Finagle 101](http://vkostyukov.net/posts/finagle-101/)
* [Finch 101](http://vkostyukov.ru/slides/finch-101/)
* Finch workshop [(slides)](https://nrinaudo.github.io/workshop-finch/#1) & [(code)](https://github.com/nrinaudo/workshop-finch)
* [Typed services using Finch](https://www.infoq.com/presentations/finch)

### Finagle

Finagle provides the main framework we use for creating services. We also use it's libraries for making outgoing connections to downstream services. You don't need to know Finagle to use Finch, but it helps if you want to tweak things (like timeouts & retries) or customise how errors are handled, etc.s

* [Getting started with Finagle](http://andrew-jones.com/blog/getting-started-with-finagle/)
* [An introduction to Finagle](http://twitter.github.io/scala_school/finagle.html)
* [Finagle examples](https://www.codatlas.com/github.com/twitter/finagle/develop)
* [Other information on Finagle](http://dirtysalt.github.io/finagle.html)

### Cats

* [Cats documentation](http://typelevel.org/cats/)

### Sangria/GraphQL

* [Learn Sangira](http://sangria-graphql.org/learn/)
* [GraphQL Queries](http://graphql.org/docs/queries/)
* [GraphiQL App](https://github.com/skevy/graphiql-app)
