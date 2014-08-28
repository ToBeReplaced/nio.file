(ns org.tobereplaced.nio.file-test
  (:require [clojure.test :refer [deftest is]]
            [org.tobereplaced.nio.file :refer [absolute-path absolute?
                                               compare-to delete!
                                               ends-with? file-name
                                               file-system normalize
                                               parent path
                                               posix-file-permission
                                               posix-file-permissions
                                               real-path register!
                                               relativize resolve-path
                                               resolve-sibling root
                                               starts-with?
                                               watch-service]])
  (:import (java.io File)
           (java.net URI)
           (java.nio.file FileSystem FileSystems)))

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
  (is (instance? FileSystem (file-system (path "foo")))))

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

;; TODO: Rewrite to use temporary directories
(deftest register!-test
  ;; in this test, a watch is placed on a directory, and a future (counter) is
  ;; created, which responds to each watched event as it occurs. We verify that
  ;; the events are watched, and each event type is delivered and handled.
  (let [watched-path "test"
        watcher (watch-service)
        watch-key (register! watched-path
                             watcher
                             #{:entry-create :entry-delete :entry-modify})
        events (atom {})
        counter (future
                  (while (not (Thread/interrupted))
                    (let [event (.take watcher)
                          event-list (.pollEvents event)]
                      (doseq [^java.nio.file.WatchEvent e event-list]
                        (swap! events update-in [(-> e .kind .name)]
                               (fnil inc 0)))
                      (.reset event))))]
    (is watch-key)
    (spit "test/foo.deleteme" "")
    (spit "test/foo.deleteme" "")
    (delete! "test/foo.deleteme")
    ;; this is needed to ensure we get all the updates to the atom
    (Thread/sleep 100)
    (is (= {"ENTRY_CREATE" 1
            "ENTRY_DELETE" 1
            "ENTRY_MODIFY" 1}
           @events))
    (future-cancel counter)))

(deftest posix-file-permission-test
  (is (= (posix-file-permission :group-execute)
         (posix-file-permission (posix-file-permission :group-execute)))
      "should convert suitable keywords into permissions"))

(deftest posix-file-permissions-test
  (is (posix-file-permissions :rw-rw-r--)
      "should be able to coerce suitable keyword"))
