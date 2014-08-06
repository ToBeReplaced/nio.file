(ns org.tobereplaced.nio.file
  "Wrapper for java.nio.file. All functions that accept a Path will be
  coerced to a Path if possible."
  (:require [org.tobereplaced.nio.file.protocols :as p])
  (:import (java.nio.file FileVisitResult FileVisitor Files LinkOption)
           (java.nio.file.attribute FileAttribute)))

;;;
;;; Definition macros, to eliminate redundancy in function
;;; definitions.
;;;

(defmacro ^:private defunarypathfn
  "Defines a function of a single path."
  [name docstring tag method]
  `(defn ~name
     ~docstring
     {:arglists '(~'[path])
      :tag ~tag}
     [p#]
     (~method (path p#))))

(defmacro ^:private defbinarypathfn
  "Defines a function of two paths."
  [name docstring tag method]
  `(defn ~name
     ~docstring
     {:arglists '(~'[path other])
      :tag ~tag}
     [p# other#]
     (~method (path p#) (path other#))))

(defmacro ^:private defcreatefn
  "Defines a create function of a path and file attributes."
  [name docstring method]
  `(defn ~name
     ~docstring
     {:arglists '(~'[path & file-attributes])
      :tag java.nio.file.Path}
     [p# & attrs#]
     (~method (path p#) (into-array FileAttribute attrs#))))

(defmacro ^:private deflinkfn
  "Defines a function of a path and link options."
  [name docstring tag method]
  `(defn ~name
     ~docstring
     {:arglists '(~'[path & link-options])
      :tag ~tag}
     [p# & options#]
     (~method (path p#) (into-array LinkOption options#))))

;;;
;;; Path creation and coercion.
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

(defn absolute-path
  "Returns an absolute path from a Path, URI, File, FileSystem and
  sequence of strings, or sequence of strings. See path for more
  details."
  {:arglists '([path] [uri] [file] [filesystem & strings] [string & strings])
   :tag java.nio.file.Path}
  [& args]
  (.toAbsolutePath ^java.nio.file.Path (apply path args)))

(deflinkfn real-path
  "Returns the real path of an existing file according to the
  link-options."
  java.nio.file.Path .toRealPath)

;;;
;;; Path functions, ordered lexicographically according to their
;;; corresponding methods.
;;;
;;; Do not need to implement .equals, .toURI, or .toString because of
;;; other clojure facilities.
;;;
;;; Do not need to implement .getName, .getNameCount, or .iterator
;;; because you can just iterate over the path as a sequence.
;;;
;;; Do not need to implement subpath because you can reduce with
;;; resolve over the path.
;;;
;;; We already implemented .toAbsolutePath and .toRealPath above.
;;;

(defbinarypathfn compare-to
  "Returns an integer comparing path to the other lexicographically."
  Integer .compareTo)

(defbinarypathfn ends-with?
  "Returns true if the path ends with the other, false otherwise."
  Boolean .endsWith)

(defunarypathfn file-name
  "Returns the name of the file or directory denoted by the path."
  java.nio.file.Path .getFileName)

(defunarypathfn file-system
  "Returns the file system that created the path."
  java.nio.file.FileSystem .getFileSystem)

(defunarypathfn parent
  "Returns the parent of the path if it has one, nil otherwise."
  java.nio.file.Path .getParent)

(defunarypathfn root
  "Returns the root of the path if it has one, nil otherwise."
  java.nio.file.Path .getRoot)

(defunarypathfn absolute?
  "Returns if the path is absolute, false otherwise"
  Boolean .isAbsolute)

(defunarypathfn normalize
  "Returns the path with redundant name elements eliminated."
  java.nio.file.Path .normalize)

;; TODO: Implement register

(defbinarypathfn relativize
  "Returns a relative path between the path and other."
  java.nio.file.Path .relativize)

(defbinarypathfn resolve-path
  "Resolves the other against the path."
  java.nio.file.Path .resolve)

(defbinarypathfn resolve-sibling
  "Resolves the other against the path's parent."
  java.nio.file.Path .resolveSibling)

(defbinarypathfn starts-with?
  "Returns true if the path starts with the other, false otherwise."
  Boolean .startsWith)

;;;
;;; File functions, ordered lexicographically according to their
;;; corresponding static methods on the Files class.
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

(defcreatefn create-directories!
  "Creates a directory by creating all nonexistent parent directories
  first."
  Files/createDirectories)

(defcreatefn create-directory!
  "Creates a new directory."
  Files/createDirectory)

(defcreatefn create-file!
  "Creates a new empty file."
  Files/createFile)

(defbinarypathfn create-link!
  "Creates a new link for an existing file."
  java.nio.file.Path Files/createLink)

;; TODO: Implement createSymbolicLink
;; TODO: Implement createTempDirectory
;; TODO: Implement createTempFile

(defunarypathfn delete!
  "Deletes the file at path."
  nil Files/delete)

(defunarypathfn delete-if-exists!
  "Deletes the file at path if it exists. Returns true if the file was
  deleted, false otherwise."
  Boolean Files/deleteIfExists)

(deflinkfn exists?
  "Returns true if the file exists, false otherwise."
  Boolean Files/exists)

;; TODO: Implement getAttribute
;; TODO: What to do about getFileAttributeView?

(defunarypathfn file-store
  "Returns the file store where the file is located."
  java.nio.file.FileStore Files/getFileStore)

(deflinkfn last-modified-time
  "Returns the last modified time for the file."
  java.nio.file.attribute.FileTime Files/getLastModifiedTime)

(deflinkfn owner
  "Returns the owner of the file."
  java.nio.file.attribute.UserPrincipal Files/getOwner)

;; TODO: Implement getPosixFilePermissions

(deflinkfn directory?
  "Returns true if the file is a directory, false otherwise."
  Boolean Files/isDirectory)

(defunarypathfn executable?
  "Returns true if the file is executable, false otherwise."
  Boolean Files/isExecutable)

(defunarypathfn hidden?
  "Returns true if the file is hidden, false otherwise."
  Boolean Files/isHidden)

(defunarypathfn readable?
  "Returns true if the file is readable, false otherwise."
  Boolean Files/isReadable)

(deflinkfn regular-file?
  "Returns true if the file is a regular file, false otherwise."
  Boolean Files/isRegularFile)

;; This could be variadic, but not sure how to make that performant.
(defbinarypathfn same-file?
  "Returns true if the two paths are the same, false otherwise."
  Boolean Files/isSameFile)

(defunarypathfn symbolic-link?
  "Returns true if the file is a symbolic link, false otherwise."
  Boolean Files/isSymbolicLink)

(defunarypathfn writable?
  "Returns true if the file is a writable, false otherwise."
  Boolean Files/isWritable)

;; TODO: Implement move
;; TODO: What to do about newBufferedReader/Writer?
;; TODO: What to do about newByteChannel/DirectoryStream?
;; TODO: What to do about newInputStream/OutputStream?

(deflinkfn not-exists?
  "Returns true if the file does not exist, false otherwise."
  Boolean Files/notExists)

(defunarypathfn probe-content-type
  "Returns true if the file is a writable, false otherwise."
  String Files/probeContentType)

(defunarypathfn read-all-bytes
  "Returns the bytes from the file."
  "[B" Files/readAllBytes)

;; TODO: Implement readAllLines
;; TODO: Implement readAttributes

(defunarypathfn read-symbolic-link
  "Returns the target of a symbolic link."
  java.nio.file.Path Files/readSymbolicLink)

;; TODO: Implement setAttribute
;; TODO: Implement setLastModifiedTime
;; TODO: Implement setOwner
;; TODO: Implement setPosixFilePermissions

(defunarypathfn size
  "Returns the size of the file in bytes."
  Long Files/size)

;; TODO: Implement write
;; TODO: Conditionally implement 1.8 features

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
  ^java.nio.file.FileVisitor
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

(defn walk-file-tree
  "Walks the file tree rooted at start with visitor. Returns the
  starting path."
  ^java.nio.file.Path
  [start visitor & {:keys [file-visit-options max-depth]
                    :or {file-visit-options #{} max-depth Integer/MAX_VALUE}}]
  (Files/walkFileTree (path start) file-visit-options max-depth visitor))
