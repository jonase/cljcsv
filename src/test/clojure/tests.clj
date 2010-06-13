(ns tests
  (:use clojure.test)
  (:require (com.github.jonase [csv :as csv])
	    (clojure.java [io :as io]))
  (:import (java.io Reader StringReader StringWriter EOFException)))

(deftest reading
  (with-open [simple-file (io/reader "src/test/clojure/simple.csv")]
    (let [csv (csv/read simple-file)]
      (is (= (count csv) 3))
      (is (= (count (first csv)) 3))))  
  (with-open [complicated-file (io/reader "src/test/clojure/complicated.csv")]
    (let [csv (csv/read complicated-file)]
      (is (= (count csv) 4))
      (is (= (count (first csv)) 5)))))

(deftest reading-and-writing
  (let [simple-file-name "src/test/clojure/simple.csv"
	simple-file-content (slurp simple-file-name)
	string-writer (StringWriter.)]
    (with-open [simple-file (io/reader simple-file-name)]
      (->> simple-file csv/read (csv/write string-writer))
      (is (= simple-file-content
	     (str string-writer))))))

(deftest throw-if-quoted-on-eof
  (let [reader (StringReader. "ab,\"de,gh\nij,kl,mn")]
    (is (thrown? RuntimeException (doall (csv/read reader))))))

