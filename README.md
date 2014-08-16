# nio.file #

Clojure wrapper for java.nio.file

Why do this? Take a look at [WHY.md].

Work in progress! Expect bugs and missing functionality until we hit
1.0.0.

## Supported Clojure and Java Versions ##

`nio.file` targets Clojure 1.6+ and Java 1.7+. New features in future
versions of Java will be implemented conditionally so that that you
can continue to use this library on 1.7.

## Maturity ##

This is alpha quality software. If your usage requires a complete and
bug free experience, please wait for 1.0.0.

## Installation ##

`nio.file` is available as a Maven artifact from [Clojars]:

```clojure
[org.tobereplaced/nio.file "0.1.0"]
```

`nio.file` follows [Semantic Versioning].  Please note that this means
the public API for this library is not yet considered stable.

## Documentation ##

Please read the [Codox API Documentation] and take a look at
[walkthrough.clj].

## What Belongs Here? ##

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

## Contributing ##

Pull requests welcome! Check out the TODO items in the source
code. Engage with me! There's a lot of work, and I don't have time for
all of it.

We need lots of tests and some guided examples in the
[walkthrough.clj].

## Support ##

Please post any comments, concerns, or issues to the Github issues
page or find me on `#clojure`.  I welcome any and all feedback.

## Changelog ##

### v0.1.0 ###

- Initial Release
- Most `Files` `FileSystem` and `Path` methods implemented.

## License ##

Copyright © 2014 ToBeReplaced

Distributed under the Eclipse Public License, the same as Clojure.
The license can be found at LICENSE in the root of this distribution.

[WHY.md]: https://github.com/ToBeReplaced/nio.file/blob/master/WHY.md
[walkthrough.clj]: https://github.com/ToBeReplaced/nio.file/blob/master/walkthrough.clj
[walkFileTree]: http://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#walkFileTree-java.nio.file.Path-java.nio.file.FileVisitor-
[Codox API Documentation]: http://ToBeReplaced.github.com/nio.file
[Clojars]: http://clojars.org/org.tobereplaced/nio.file
[Semantic Versioning]: http://semver.org
