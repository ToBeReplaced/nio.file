(ns org.tobereplaced.nio.file
  (:require [org.tobereplaced.nio.file.protocols :as p])
  (:import (java.nio.file FileVisitResult FileVisitor Files LinkOption
                          Path)))

;;;
;;; Path creation and manipulation
;;;

(defn path
  "Returns a Path from a Path, URI, File, FileSystem and sequence of
  strings, or sequence of strings. This will not accept paths in place
  of strings for variadic usage because the behavior is not well
  defined. Consider using resolve-path.

  This function is extensible through the UnaryPath and NaryPath
  protocols."
  {:arglists '([path] [uri] [file] [filesystem & strings] [string & strings])
   :tag java.nio.file.Path}
  ([this] (p/unary-path this))
  ([this & strings] (p/nary-path this strings)))

(defn compare-to
  "Returns an int comparing path to the other lexicographically. Other
  must be able to be coerced to a path."
  {:arglists '([path other])}
  [^Path p other]
  (.compareTo p (path other)))

(defn starts-with?
  "Returns true if the path starts with the other, false
  otherwise. Other must be able to be coerced to a path."
  {:arglists '([path other])}
  [^Path p other]
  (.startsWith p (path other)))

(defn ends-with?
  "Returns true if the path ends with the other, false
  otherwise. Other must be able to be coerced to a path."
  {:arglists '([path other])}
  [^Path p other]
  (.endsWith p (path other)))

(defn relativize
  "Returns a relative path between the path and other. Other must be
  able to be coerced to a path."
  {:arglists '([path other])
   :tag java.nio.file.Path}
  [^Path p other]
  (.relativize p (path other)))

(defn resolve-path
  "Resolves the others against the path in order. The others must be
  able to be coerced to paths."
  {:arglists '([path & others])
   :tag java.nio.file.Path}
  [^Path p & others]
  (reduce #(.resolve ^Path %1 (path %2)) p others))

(defn resolve-sibling
  "Resolves the other against the path's parent. Other must be able to
  be coerced to a path."
  {:arglists '([path other])
   :tag java.nio.file.Path}
  [^Path p other]
  (.resolveSibling p (path other)))

(defn- link-options
  "Returns an array of the LinkOptions."
  ^"[Ljava.nio.file.LinkOption;"
  [& options]
  (into-array LinkOption options))

(defn real-path
  "Returns the real path of an existing file according to the
  link-options."
  {:arglists '([path link-options])
   :tag java.nio.file.Path}
  [^Path p & options]
  (.toRealPath p (apply link-options options)))

;;;
;;; File methods
;;;

(defn copy
  "Copy all bytes from a file to a file, file to an output stream, or
  input stream to a file. The return type depends on the form of
  copy. Copying to or from a stream will return a long of the number
  of bytes read or written. Copying a file to another file will return
  the path to the target. If the source or target are not streams,
  they will be coerced to paths. Copy options may be included for
  configuration when writing to a file.

  This function is extensible through the Copy, CopyFromInputStream,
  and CopyFromPath protocols."
  {:arglists (list '[source out]
                   '[in target & copy-options]
                   '[source target & copy-options])}
  [source target & copy-options]
  (p/copy source target copy-options))

(defn delete!
  "Deletes the file at the location. The location must be able to be
  coerced to a path."
  [location]
  (Files/delete (path location)))

(defn file-visitor
  "Returns a reified FileVisitor that acts as a SimpleFileVisitor with
  methods overridden by the functions passed in."
  ^java.nio.file.FileVisitor
  [& {:keys [pre-visit-directory post-visit-directory
             visit-file visit-file-failed]
      :or {pre-visit-directory (constantly FileVisitResult/CONTINUE)
           post-visit-directory (fn [_ exc]
                                  (if exc
                                    (throw exc)
                                    FileVisitResult/CONTINUE))
           visit-file (constantly FileVisitResult/CONTINUE)
           vist-file-failed (fn [_ exc] (throw exc))}}]
  (reify FileVisitor
    (preVisitDirectory [_ dir attrs] (pre-visit-directory dir attrs))
    (postVisitDirectory [_ dir exc] (post-visit-directory dir exc))
    (visitFile [_ file attrs] (visit-file file attrs))
    (visitFileFailed [_ file exc] (visit-file-failed file exc))))

(defn naive-visitor
  "Returns a reified FileVisitor that acts as a SimpleFileVisitor with
  functions called with only the first argument of its corresponding
  method. pre-visit-directory and post-visit-directory will be called
  with only the directory. visit-file will be called with only the
  file. Exceptions will be thrown if they exist, so you may not
  override visitFileFailed. Attributes will be ignored. Each function
  must return a FileVisitResult or nil. If nil,
  FileVisitResult/CONTINUE will be used."
  [& {:keys [pre-visit-directory post-visit-directory visit-file]
      :or {pre-visit-directory (constantly nil)
           post-visit-directory (constantly nil)
           visit-file (constantly nil)}}]
  (let [continue (fn [f] #(if-some [res (f %)] res FileVisitResult/CONTINUE))
        drop-and-continue (fn [f]
                            (let [g (continue f)]
                              (fn [x _]
                                (g x))))
        raise-or-continue (fn [f]
                            (let [g (continue f)]
                              (fn [x exc]
                                (if exc (throw exc) (g x)))))]
    (file-visitor :pre-visit-directory
                  (drop-and-continue pre-visit-directory)
                  :post-visit-directory
                  (raise-or-continue post-visit-directory)
                  :visit-file
                  (drop-and-continue visit-file))))
