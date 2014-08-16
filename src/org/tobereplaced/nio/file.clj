(ns org.tobereplaced.nio.file
  "Wrapper for java.nio.file. All functions that accept a Path will be
  coerced to a Path if possible. All functions that accept a
  FileSystem will be coerced to a FileSystem if
  possible. Additionally, all functions that accept a FileSystem can
  accept no argument in exchange for the default
  FileSystem."
  (:require [org.tobereplaced.nio.file.protocols :as p])
  (:import (java.nio.charset StandardCharsets)
           (java.nio.file CopyOption FileSystems FileVisitResult
                          FileVisitor Files LinkOption WatchEvent$Kind
                          WatchEvent$Modifier WatchService)
           (java.nio.file.attribute FileAttribute FileAttributeView
                                    UserPrincipalLookupService)))

;;;
;;; Definition macros, to eliminate redundancy in function
;;; definitions.
;;;

(defmacro ^:private defpathfn
  "Defines a function of a single path."
  [name docstring tag method & args]
  (let [fn-args (repeat (count args) (gensym))]
    `(defn ~name
       ~docstring
       {:arglists '(~(vec (concat '[path] args)))
        :tag ~tag}
       [p# ~@fn-args]
       (~method (path p#) ~@fn-args))))

(defmacro ^:private defbinarypathfn
  "Defines a function of exactly two paths."
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
  "Defines a function of a path, extra arguments, and link options."
  [name docstring tag method & args]
  (let [fn-args (repeat (count args) (gensym))]
    `(defn ~name
       ~docstring
       {:arglists '(~(vec (concat '[path] args '[& link-options])))
        :tag ~tag}
       [p# ~@fn-args & options#]
       (~method (path p#) ~@fn-args (into-array LinkOption options#)))))

(defmacro ^:private deffsfn
  "Defines a function on a filesystem."
  [name docstring tag method]
  `(defn ~name
     ~docstring
     {:arglists '(~'[] ~'[fs])
      :tag ~tag}
     ([] (~name (file-system)))
     ([fs#] (~method (file-system fs#)))))

;;;
;;; Creation and coercion for Paths, FileSystems, and
;;; WatchEvent.Kinds.
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

(defn file-system
  "Returns the FileSystem located at the URI, the FileSystem used to
  create the Path, or the default FileSystem when called with no
  arguments. Passing in a FileSystem returns itself.

  This function is extensible through the FileSystem protocol."
  ;; TODO: Implement FileSystems/newFileSystem as part of file-system
  ;; and add notes about being closeable.
  {:arglists '([] [path] [uri] [fs])
   :tag java.nio.file.FileSystem}
  ([] (FileSystems/getDefault))
  ([this] (p/file-system this)))

(defn watch-event-kind
  "Returns a WatchEvent.Kind from the keyword. The keyword may
  correspond to any of the StandardWatchEventKinds. Passing in a
  WatchEvent.Kind returns itself.

  This function is extensible through the WatchEventKind protocol."
  {:arglists '([:entry-create] [:entry-delete] [:entry-modify] [event-kind])
   :tag java.nio.file.WatchEvent$Kind}
  ([this] (p/watch-event-kind this)))

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
;;; We already implemented .toAbsolutePath, .toRealPath, and
;;; .getFileSystem above.
;;;

(defbinarypathfn compare-to
  "Returns an integer comparing path to the other lexicographically."
  Integer .compareTo)

(defbinarypathfn ends-with?
  "Returns true if the path ends with the other, false otherwise."
  Boolean .endsWith)

(defpathfn file-name
  "Returns the name of the file or directory denoted by the path."
  java.nio.file.Path .getFileName)

(defpathfn parent
  "Returns the parent of the path if it has one, nil otherwise."
  java.nio.file.Path .getParent)

(defpathfn root
  "Returns the root of the path if it has one, nil otherwise."
  java.nio.file.Path .getRoot)

(defpathfn absolute?
  "Returns if the path is absolute, false otherwise"
  Boolean .isAbsolute)

(defpathfn normalize
  "Returns the path with redundant name elements eliminated."
  java.nio.file.Path .normalize)

(defn register!
  "Registers the file located by the path with the watch service and
  returns a WatchKey."
  {:arglists '([path watcher events & modifiers])
   :tag java.nio.file.WatchKey}
  [p watcher events & modifiers]
  (.register (path p)
             watcher
             (into-array WatchEvent$Kind (map watch-event-kind events))
             (into-array WatchEvent$Modifier modifiers)))

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

  This function is extensible through the Copy protocol."
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

(defn create-symbolic-link!
  "Creates a symbolic link form path to target."
  {:arglists '([path target & attrs])
   :tag java.nio.file.Path}
  [p target & attrs]
  (Files/createSymbolicLink (path p) (path target)
                            (into-array FileAttribute attrs)))

(defn create-temp-directory!
  "Creates a temporary directory with the given prefix in the given
  directory or the default temporary directory if none is provided."
  {:arglists '([path prefix & attrs] [prefix & attrs])}
  [this & more]
  (p/create-temp-directory (first more) this (rest more)))

(defn create-temp-file!
  "Creates a temporary file with the given prefix and suffix in the
  given directory or the default temporary directory if none is
  provided.

  Unlike the Files/createTempFile implementation, you may not pass nil
  as the suffix when specifying a directory. This is because we would
  be unable to determine if a string in the first argument is intended
  to be a path to the directory or a prefix."
  {:arglists (list '[prefix]
                   '[prefix suffix]
                   '[prefix suffix & attrs]
                   '[dir prefix suffix & attrs])}
  [this & [x y & more]]
  (p/create-temp-file y this x  more))

(defpathfn delete!
  "Deletes the file at path."
  nil Files/delete)

(defpathfn delete-if-exists!
  "Deletes the file at path if it exists. Returns true if the file was
  deleted, false otherwise."
  Boolean Files/deleteIfExists)

(deflinkfn exists?
  "Returns true if the file exists, false otherwise."
  Boolean Files/exists)

(deflinkfn attribute
  "Returns the value of a file attribute."
  Object Files/getAttribute attribute)

(deflinkfn file-attribute-view
  "Returns a file attribute view of the given type."
  FileAttributeView Files/getFileAttributeView attribute-view-type)

(defpathfn file-store
  "Returns the file store where the file is located."
  java.nio.file.FileStore Files/getFileStore)

(deflinkfn last-modified-time
  "Returns the last modified time for the file."
  java.nio.file.attribute.FileTime Files/getLastModifiedTime)

(deflinkfn owner
  "Returns the owner of the file."
  java.nio.file.attribute.UserPrincipal Files/getOwner)

(deflinkfn posix-file-permissions
  "Returns the POSIX file permissions for the file."
  ;; TODO: How to type hint a set of PosixFilePermissions?
  nil Files/getPosixFilePermissions)

(deflinkfn directory?
  "Returns true if the file is a directory, false otherwise."
  Boolean Files/isDirectory)

(defpathfn executable?
  "Returns true if the file is executable, false otherwise."
  Boolean Files/isExecutable)

(defpathfn hidden?
  "Returns true if the file is hidden, false otherwise."
  Boolean Files/isHidden)

(defpathfn readable?
  "Returns true if the file is readable, false otherwise."
  Boolean Files/isReadable)

(deflinkfn regular-file?
  "Returns true if the file is a regular file, false otherwise."
  Boolean Files/isRegularFile)

;; This could be variadic, but not sure how to make that performant.
(defbinarypathfn same-file?
  "Returns true if the two paths are the same, false otherwise."
  Boolean Files/isSameFile)

(defpathfn symbolic-link?
  "Returns true if the file is a symbolic link, false otherwise."
  Boolean Files/isSymbolicLink)

(defpathfn writable?
  "Returns true if the file is a writable, false otherwise."
  Boolean Files/isWritable)

(defn move
  "Move the file at source to target. Returns the target path. "
  {:tag java.nio.file.Path}
  [source target & copy-options]
  (Files/move (path source) (path target) (into-array CopyOption copy-options)))

;; TODO: What to do about newBufferedReader/Writer?
;; TODO: What to do about newByteChannel/DirectoryStream?
;; TODO: What to do about newInputStream/OutputStream?

(deflinkfn not-exists?
  "Returns true if the file does not exist, false otherwise."
  Boolean Files/notExists)

(defpathfn probe-content-type
  "Returns true if the file is a writable, false otherwise."
  String Files/probeContentType)

(defpathfn read-all-bytes
  "Returns the bytes from the file."
  "[B" Files/readAllBytes)

(defn read-all-lines
  "Returns the lines of a file."
  {:arglists '([path] [path charset])}
  ;; We fill this in for 1.7-to-1.8 compatibility.
  ([p] (Files/readAllLines (path p) StandardCharsets/UTF_8))
  ([p cs] (Files/readAllLines (path p) cs)))

(defn read-attributes
  "Returns the file's attributes."
  {:arglists (list '[path attribute-type & link-options]
                   '[path attribute-string & link-options])}
  [p attributes & options]
  (p/read-attributes attributes (path p) options))

(defpathfn read-symbolic-link
  "Returns the target of a symbolic link."
  java.nio.file.Path Files/readSymbolicLink)

(deflinkfn set-attribute!
  "Sets the value of a file attribute."
  java.nio.file.Path Files/setAttribute attribute value)

(defpathfn set-last-modified-time!
  "Sets the last modified time of the file."
  java.nio.file.Path Files/setLastModifiedTime file-time)

(defpathfn set-owner!
  "Sets the file's owner."
  java.nio.file.Path Files/setOwner owner)

(defpathfn set-posix-file-permissions!
  "Sets the file's POSIX permissions."
  java.nio.file.Path Files/setPosixFilePermissions permissions)

(defpathfn size
  "Returns the size of the file in bytes."
  Long Files/size)

(defn write
  "Write bytes or lines of text to a file. Open options may be
  included for configuration when opening or creating a file. In the
  case of writing lines of text, the charset will default to UTF-8
  when not provided, as per the updates in Java 1.8."
  {:arglists (list '[path bytes & open-options]
                   '[path lines & open-options]
                   '[path lines charset & open-options])}
  [p items & more]
  (p/write items (path p) more))

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

;;;
;;; FileSystem functions, ordered lexicographically according to their
;;; corresponding methods on the FileSystem class.
;;;
;;; Do not need to implement .close because of other clojure
;;; facilities.
;;;
;;; We already implemented .getPath above.
;;;

(deffsfn file-stores
  "Returns an iterable of the FileStores of a file system."
  ;; TODO: Is there any kind of type hint we can provide for this?
  nil
  .getFileStores)

;; TODO: Implement getPathMatcher

(deffsfn root-directories
  "Returns an iterable of the paths of the root directories of the
  file system"
  ;; TODO: What is the type-hint for an Iterable of Paths?
  nil
  .getRootDirectories)

(deffsfn separator
  "Returns the name separator of the file system."
  String
  .getSeparator)

(deffsfn user-principal-lookup-service
  "Returns the UserPrincipalLookupService for the filesystem."
  UserPrincipalLookupService
  .getUserPrincipalLookupService)

(deffsfn open?
  "Returns true if the file system is open, false otherwise."
  Boolean
  .isOpen)

(deffsfn read-only?
  "Returns true if the file system is read-only, false otherwise."
  Boolean
  .isReadOnly)

;; TODO: There should be an extension library that reifies a
;; WatchService from a platform based WatchService and acts like a
;; core.async channel.
(deffsfn watch-service
  "Returns a new WatchService for the file system. Should be used
  inside with-open to ensure the WatchService is properly closed."
  WatchService
  .newWatchService)

(deffsfn provider
  "Returns the FileSystemProvider corresponding to the file system."
  java.nio.file.spi.FileSystemProvider
  .provider)

(deffsfn supported-file-attribute-views
  "Returns a set of names of file attribute views supported by the
  file system."
  nil
  .supportedFileAttributeViews)

;;;
;;; TODO: Implement FileSystemProvider methods that aren't delegated
;;; by Files.
;;;
;;; TODO: Implement UserPrincipalLookupService methods and coercion.
;;;
