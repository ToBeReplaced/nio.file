(ns org.tobereplaced.nio.file-test
  (:require [clojure.test :as test :refer [deftest is]]
            [org.tobereplaced.nio.file :refer [path compare-to starts-with?
                                               ends-with? relativize
                                               resolve-path resolve-sibling
                                               real-path copy delete!
                                               naive-visitor absolute-path
                                               file-name file-system parent
                                               root absolute? normalize
                                               relativize]])
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

(deftest absolute-path-test
  (is (= (absolute-path "foo/bar")
         (path (System/getProperty "user.dir") "foo" "bar"))))

(deftest real-path-test
  (is (= (absolute-path "project.clj")
         (real-path "./project.clj")
         (real-path "./././project.clj")))
  (is (thrown-with-msg? java.nio.file.NoSuchFileException #"foo/bar"
                        (real-path "foo/bar"))))

(deftest compare-to-test
  (is (= -1
         (compare-to (path "a")
                     (path "b"))
         (compare-to (path "0")
                     (path "1"))))
  (is (= 1
         (compare-to (path "b")
                     (path "a"))
         (compare-to (path "1")
                     (path "0"))))
  (is (= 0
         (compare-to (path "foo/bar")
                     (path "foo" "bar")))))

(deftest ends-with?-test
  (is (ends-with? (path "foo/bar/baz")
                  (path "bar/baz")))
  (is (not (ends-with? (path "foo/bar/baz.clj")
                       (path ".clj")))))

(deftest file-name-test
  (is (= (file-name (path "baz.clj"))
         (file-name (path "bar/baz.clj"))
         (file-name (path "foo/bar/baz.clj"))))
  (is (not (= "baz.clj"
              (file-name (path "baz.clj"))))))

(deftest file-system-test
  ;; TODO: is there anything more interesting we can check here?
  (is (instance? java.nio.file.FileSystem (file-system (path "foo")))))


(deftest parent-test
  (is (= (path "foo/bar/")
         (parent (path "foo/bar/baz")))))

(deftest root-test
  (is (= (path "/")
         (root (path "/foo/bar/baz")))))

(deftest absolute?-test
  (is (absolute? (path "/foo")))
  (is (not (absolute? (path "foo")))))

(deftest normalize-test
  (is (= (normalize (path "foo/../foo/../foo/bar"))
         (path "foo/bar"))))

(deftest relativize-test
  (is (= (relativize (path "foo/bar/baz") (path "foo"))
         (path "../..")))
  (is (= (relativize (path "foo/baz") (path "foo/quux"))
         (path "../quux"))))

(deftest resolve-path-test
  (is (= (resolve-path (path "foo/bar/baz") (path "quux"))
         (path "foo/bar/baz/quux")))
  (is (= (normalize (resolve-path (path "foo/bar/baz") (path "../../")))
         (path "foo/"))))

(deftest resolve-sibling-test
  (is (= (resolve-sibling (path "foo/bar") (path "quux"))
         (path "foo/quux"))))

(deftest starts-with?-test
  (is (starts-with? (path "foo/bar") (path "foo")))
  (is (not (starts-with? (path "foo/bar") (path "f")))))
