(ns org.tobereplaced.nio.file.protocols
  (:import (java.io File InputStream OutputStream)
           (java.net URI)
           (java.nio.file CopyOption FileSystem Files Path Paths)))

(def ^:private empty-string-array (into-array String []))

(defprotocol UnaryPath
  (unary-path [this]))

(extend-protocol UnaryPath
  Path
  (unary-path [this] this)
  File
  (unary-path [this] (.toPath this))
  URI
  (unary-path [this] (Paths/get this))
  String
  (unary-path [this] (Paths/get this empty-string-array)))

(defprotocol NaryPath
  (nary-path [this more]))

(extend-protocol NaryPath
  FileSystem
  (nary-path [this [s & more]] (.getPath this s (into-array String more)))
  String
  (nary-path [this more] (Paths/get this (into-array String more))))

(defprotocol CopyFromInputStream
  (copy-from-input-stream [this source options]))

(extend-protocol CopyFromInputStream
  Path
  (copy-from-input-stream [this ^InputStream source options]
    (Files/copy source this
                ^"[Ljava.nio.file.CopyOption;"
                (into-array CopyOption options)))
  Object
  (copy-from-input-stream [this source options]
    (copy-from-input-stream (unary-path this) source options)))

(defprotocol CopyFromPath
  (copy-from-path [this source options]))

(extend-protocol CopyFromPath
  OutputStream
  (copy-from-path [this source _]
    (Files/copy source this))
  Path
  (copy-from-path [this ^Path source options]
    (Files/copy source this
                ^"[Ljava.nio.file.CopyOption;"
                (into-array CopyOption options)))
  Object
  (copy-from-path [this source options]
    (copy-from-path (unary-path this) source options)))

(defprotocol Copy
  (copy [this target options]))

(extend-protocol Copy
  InputStream
  (copy [this target options]
    (copy-from-input-stream target this options))
  Path
  (copy [this target options]
    (copy-from-path target this options))
  Object
  (copy [this target options]
    (copy (unary-path this) target options)))
