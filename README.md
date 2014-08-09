# nio.file #

Clojure wrapper for java.nio.file

Work in progress. No release scheduled.

Pull requests welcome! Check out the TODO items in the source code.

# Why Wrap java.nio.file? #

The classes and interfaces in java.nio.file form an *excellent* file
system API. They allow you to do everything you want to a file system
without the assumptions that plague the old java APIs or most APIs in
other languages. Symbolic links? No problem!

So, why aren't we using it in Clojure? It's *hard*.


## Problem: Variadic Arguments ##

Many methods in java.nio.file use variadic arguments of new types like
`LinkOption` and `CopyOption`. In order to call these from clojure,
you must create java arrays containing those elements with commands
like `(into-array CopyOption options)`. In doing so, we require calls
that have no specific options to include extra information. Moreover,
unless you're used to writing type-hints for java arrays, odds are you
are going to end up reflecting often.

This library exposes these variadic argument methods as clojure
functions with variadic arguments, eliminating the need for a user to
create java arrays.

## Problem: Polymorphism ##

Some methods accept a String or a Path, and others accept some
combination of InputStreams, OutputStreams, Paths, and
CopyOptions. These are counter to usability in clojure and can be
difficult to type-hint appropriately in applications that require it.

This library provides coercion-based constructor methods that are
extensible through protocols and uses them to create polymorphic
versions of the methods in java.nio.file.

## Problem: FileVisitor ##

Methods like [walkFileTree] require a `FileVisitor` as an argument,
which can be difficult to construct.

This library provides functions to create reified instances of a
`FileVisitor` from user-provided functions.

# What belongs here? #

The goal of this library is to make it so that everything you *could*
do with java.nio.file, you can do easier with this library.

Towards this goal, if you find something that you must reach directly
to java.nio.file for, please file an issue. It must be rectified.

This is not a utility library. This library is only here to make
java.nio.file easier to use. Consequently, most functions herein are
just wrappers around existing java methods with coercion where
appropriate. The rest of the library is made up of constructor
functions to implement required interfaces for arguments to the
existing java methods.

As an example, implementing "remove-directory" is out of
scope. However, [walkthrough.clj] will show you how to do that and
other utility-like-things quite easily.

# Walkthrough #

See [walkthrough.clj] for examples of using this library.

[walkthrough.clj]: https://github.com/ToBeReplaced/nio.file/blob/master/walkthrough.clj
[walkFileTree]: http://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#walkFileTree-java.nio.file.Path-java.nio.file.FileVisitor-
