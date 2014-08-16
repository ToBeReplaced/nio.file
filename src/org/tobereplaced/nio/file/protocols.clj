(ns org.tobereplaced.nio.file.protocols
  (:import (clojure.lang Keyword)
           (java.io File InputStream OutputStream)
           (java.net URI)
           (java.nio.charset Charset StandardCharsets)
           (java.nio.file CopyOption FileSystems Files LinkOption
                          OpenOption Path Paths StandardWatchEventKinds
                          WatchEvent$Kind)
           (java.nio.file.attribute FileAttribute)))

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
  java.nio.file.FileSystem
  (nary-path [this [s & more]] (.getPath this s (into-array String more)))
  String
  (nary-path [this more] (Paths/get this (into-array String more))))

(defprotocol FileSystem
  (file-system [this]))

(extend-protocol FileSystem
  java.nio.file.FileSystem
  (file-system [this] this)
  URI
  (file-system [this] (FileSystems/getFileSystem this))
  Path
  (file-system [this] (.getFileSystem this)))

(defprotocol WatchEventKind
  (watch-event-kind [this]))

(extend-protocol WatchEventKind
  WatchEvent$Kind
  (watch-event-kind [this] this)
  Keyword
  (watch-event-kind [this]
    (or (get {:entry-create StandardWatchEventKinds/ENTRY_CREATE
              :entry-delete StandardWatchEventKinds/ENTRY_DELETE
              :entry-modify StandardWatchEventKinds/ENTRY_MODIFY}
             this)
        (->> this
             (format "No StandardWatchEventKind found for keyword: %s")
             IllegalArgumentException.
             throw))))

(defprotocol ^:private CopyFromInputStream
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

(defprotocol ^:private CopyFromPath
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

(defprotocol ^:private CreateTempDirectory
             (create-temp-directory [prefix dir attrs]))

(extend-protocol CreateTempDirectory
  nil
  (create-temp-directory [_ prefix attrs]
    (Files/createTempDirectory prefix (into-array FileAttribute attrs)))
  FileAttribute
  (create-temp-directory [attr prefix attrs]
    (Files/createTempDirectory prefix
                               (into-array FileAttribute (cons attr attrs))))
  String
  (create-temp-directory [prefix dir attrs]
    (Files/createTempDirectory (unary-path dir) prefix
                               (into-array FileAttribute attrs))))

(defprotocol ^:private CreateTempFile
             (create-temp-file [suffix dir prefix attrs]))

(extend-protocol CreateTempFile
  nil
  (create-temp-file [_ prefix suffix attrs]
    (Files/createTempFile prefix suffix
                          (into-array FileAttribute attrs)))
  FileAttribute
  (create-temp-file [attr prefix suffix attrs]
    (Files/createTempFile prefix suffix
                          (into-array FileAttribute (cons attr attrs))))
  String
  (create-temp-file [suffix dir prefix attrs]
    (Files/createTempFile (unary-path dir) prefix suffix
                          (into-array FileAttribute attrs))))

(defprotocol ^:private ReadAttributes
             (read-attributes [this path options]))

(extend-protocol ReadAttributes
  Class
  (read-attributes [this path options]
    (Files/readAttributes ^Path path this
                          ^"[Ljava.nio.file.LinkOption;"
                          (into-array LinkOption options)))
  String
  (read-attributes [this path options]
    (Files/readAttributes ^Path path this
                          ^"[Ljava.nio.file.LinkOption;"
                          (into-array LinkOption options))))

(defprotocol ^:private WriteLines
             (write-lines [this path lines options]))

(extend-protocol WriteLines
  Charset
  (write-lines [this path lines options]
    (Files/write path lines this (into-array OpenOption options)))
  OpenOption
  (write-lines [this path lines options]
    (write-lines StandardCharsets/UTF_8 path lines (cons this options)))
  nil
  (write-lines [_ path lines options]
    (write-lines StandardCharsets/UTF_8 path lines options)))

(defprotocol Write
  (write [this path options]))

(extend-protocol Write
  (Class/forName "[B")
  (write [this ^Path path options]
    (Files/write path
                 ^"[B" this
                 ^"[Ljava.nio.file.OpenOption;"
                 (into-array OpenOption options)))
  Iterable
  (write [this path options]
    (write-lines (first options) path this (rest options))))
