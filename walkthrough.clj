(ns walkthrough
  (:require [org.tobereplaced.nio.file :refer [delete! naive-visitor path]])
  (:import [java.nio.file Files]))

;; Delete an entire directory tree without following symlinks.
(Files/walkFileTree (path "out")
                    (naive-visitor :post-visit-directory delete!
                                   :visit-file delete!))
