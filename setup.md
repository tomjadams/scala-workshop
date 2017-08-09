# Setup

To begin, you're probably going to want to create a git repo to track your changes. Go ahead and do this.

The first thing you're going to want to do is install Java. Scala is a JVM language, which means that its compiler emits bytecode, which runs on a JVM. If you're on a Mac, you can probably just open a shell & type `java`. Though it's probably better to download from [the source](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). You will need a JDK (not a JRE), as of the time of writing this is "Java SE Development Kit 8u144". There is also [documentation](http://www.oracle.com/technetwork/java/javase/documentation/jdk8-doc-downloads-2133158.html) available (handy for linking into your IDE).

Then, you're going to want to download & install [sbt](http://www.scala-sbt.org). If you're on Mac, the easiest way is probably to `brew install sbt`, or you may wish to copy a version of [this runner](https://github.com/paulp/sbt-extras) locally (which is my preference) to the root of your project. Or, you can [follow the instructions](http://www.scala-sbt.org/0.13/docs/Setup.html).

So the first thing you're going to want to do is get a basic build going. You can follow along to the [documentation](http://www.scala-sbt.org/0.13/docs/Hello.html) to do this (ensure you have the correct path), but it will be something like:

```
$ ./sbt new sbt/scala-seed.g8
...
Minimum Scala build. 

name [My Something Project]: Take Home Test

Template applied in ./take-home-test
```

What this will do is bootstrap sbt by downloading the latest version, then create a new project using the values you've given it. You will also get some supporting code, such as the Scala runtime (to run sbt).

You will now have a basic project structure:

```
$ ls -l take-home-test
total 8
-rw-rw----  1 tom  staff  282 Aug  9 20:58 build.sbt
drwxrwx---  4 tom  staff  136 Aug  9 20:58 project
drwxrwx---  4 tom  staff  136 Aug  9 20:58 src
```

These do the following:

* `build.sbt` - This is the project build file, it contains information about the project, etc.
* `project` - This directory contains supporting files for the build, including configuring the build itself (e.g. sbt plugins that give your build additional functionality).
* `src` - This is where your source code goes, including your tests. If you're familiar with Java, sbt uses the Maven style of directory structure (by default).

Let's go ahead & edit the build file, and update the Scala version to 2.12.3.

`sbt` can run both as a command or interactively (REPL). It's up to you how you want to use it, the REPL is quite nice, and allows you to interactively (with tab completion, etc.) run commands.

Let's run the app in the sbt console (sbt might do some building of things the first time):

```
$ cd take-home-test
$ ./sbt
> run
[info] Updating {file:/Users/tom/src/test/take-home-test/}root...
[info] Resolving jline#jline;2.14.1 ...
[info] Done updating.
[info] Compiling 1 Scala source to /Users/tom/src/test/take-home-test/target/scala-2.12/classes...
[info] Running example.Hello 
hello
[success] Total time: 5 s, completed 09/08/2017 9:40:27 PM
> exit
```

Yay!

So this has tried to run the app, in order to do this it's compile the app first.

OK, you've just got yourself a basic Scala app up & running!