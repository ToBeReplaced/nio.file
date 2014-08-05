(ns walkthrough
  (:require [org.tobereplaced.nio.file :refer [delete! naive-visitor
                                               walk-file-tree]]))

;; Delete an entire directory tree without following symlinks.
(walk-file-tree "out"
                (naive-visitor :post-visit-directory delete!
                               :visit-file delete!))
