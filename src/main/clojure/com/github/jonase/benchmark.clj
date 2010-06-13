(ns ^{:author "Jonas Enlund"
      :doc "Benchmarking cljcsv."}
  com.github.jonase.benchmark
  (:require [com.github.jonase [csv :as csv]]
	    [clojure.java [io :as io]]))

(def *resource-dir* "resources/")

(defn- gencsv-seq [rows cols f]
  (->> (repeatedly f)
       (partition cols)
       (take rows)))

(defn rand-chr
  "Random lowercase character"
  []
  (char (+ 97 (rand-int (- 122 97)))))

(defn rand-str
  "Random string of length n"
  [n]
  (apply str (take n (repeatedly rand-chr))))

(defn gencsv-files []
  (with-open [fm (io/writer (str *resource-dir* "fm.csv"))
	      mf (io/writer (str *resource-dir* "mf.csv"))
	      mm (io/writer (str *resource-dir* "mm.csv"))
	      lc (io/writer (str *resource-dir* "lc.csv"))]
    ;; 10x10000
    (csv/write fm (gencsv-seq 10 10000 rand))
    ;; 10000x10
    (csv/write mf (gencsv-seq 10000 10 rand))
    ;; 1000x100
    (csv/write mm (gencsv-seq 1000 100 rand))
    ;; 20x10 (with 10000 character long cells)
    (csv/write lc (gencsv-seq 20 10 #(rand-str 10000)))))

;; Endless supply of readers for a file
(defn- readers [file]
  (atom (repeatedly #(io/reader file))))

;; get the next reader
(defn- next-reader [reader-atom]
  (let [reader (first @reader-atom)]
    (swap! reader-atom next)
    reader))

(defmacro time*
  "Evaluates expr. Returns how long it took, in ms"
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (double (- (. System (nanoTime)) start#)) 1000000.0)))

(defn- avg
  "Calculates the avarage of nums after removing the largest/smallest
   elements"
  [nums]
  (/ (- (reduce + nums)
	(reduce max nums)
	(reduce min nums))
     (- (count nums) 2)))

(defn- filesize [file]
  "filesize, in MB"
  (double (/ (.length file)
	     (* 1024 1024))))

(defn- bench-reading [file]
  "Reads the csv file 10 times, measuring the time it takes. Returns
   the avarage of the times after removing fastest and slowest runs."
  (let [rds (readers file)]
    (loop [times [] count 10]
      (if (pos? count)
	(recur (conj times (with-open [reader (next-reader rds)]
			     (time* (doall (csv/read reader)))))
	       (dec count))
	(avg times)))))

(defn run-benchmark []
  "Run the benchmark, creating files if needed."
  (let [fm (io/file (str *resource-dir* "fm.csv"))
	mf (io/file (str *resource-dir* "mf.csv"))
	mm (io/file (str *resource-dir* "mm.csv"))
	lc (io/file (str *resource-dir* "lc.csv"))]
    (when-not (and (.exists fm) (.exists mf) (.exists mm) (.exists lc))
      (println "Creating benchmark files...")
      (.mkdirs (io/file *resource-dir*))
      (gencsv-files)
      (println "done."))
    (println "csvclj benchmark")
    (println "================")
    (println "1. Reading")
    (println (format "1.1. Many cells/record (%.1f MB): %.2fms" (filesize fm) (bench-reading fm)))
    (println (format "1.2. Many records (%.1f MB): %.2fms" (filesize mf) (bench-reading mf)))
    (println (format "1.3. Many records & cells (%.1f MB): %.2fms" (filesize mm) (bench-reading mm)))
    (println (format "1.4. Long cells (%.1f MB): %.2fms" (filesize lc) (bench-reading lc)))))