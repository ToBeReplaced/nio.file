(ns org.tobereplaced.nio.file-test
  (:require [clojure.test :refer [deftest is]]
            [org.tobereplaced.nio.file :refer [path compare-to starts-with?
                                               ends-with? relativize
                                               resolve-path resolve-sibling
                                               real-path copy delete!
                                               naive-visitor]])
  (:import (java.net URI)
           (java.io File)
           (java.nio.file FileSystems)))

(deftest path-test
  (is (every? #(= (path "/foo/bar") (apply path %))
              [["/foo/bar"]
               ["/foo" "/bar"]
               ["/foo/" "bar"]
               ["/foo" "bar"]
               [(path "/foo/bar")]
               [(File. "/foo/bar")]
               [(URI. "file:///foo/bar")]
               [(FileSystems/getDefault) "/foo" "bar"]])
      "should coerce many equivalent forms appropriately"))
