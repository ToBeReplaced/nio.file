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

## Problem: Interfaces ##

Methods like [walkFileTree] require a `FileVisitor` as an argument,
which can be difficult to construct.

This library provides functions to create reified instances of a
`FileVisitor` from user-provided functions.
