;; Copyright (c) Jonas Enlund. All rights reserved.  The use and
;; distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ^{:author "Jonas Enlund"
      :doc "Reading and writing comma separated values."}
  com.github.jonase.csv
  (:refer-clojure :exclude [read])
  (:require (clojure [string :as str]))
  (:import (java.io Reader Writer)))

(set! *warn-on-reflection* true)

(def ^{:private true}
     lf (int \newline))
(def ^{:private true}
     cr (int \return))
(def ^{:private true}
     eof -1)

(defn- read-cell [^Reader reader sep quote]
  (let [sb (StringBuilder.)]
    (loop [ch (.read reader) in-quotes? false]
      (condp == ch
	  
	sep
	(if in-quotes?
	  (do (.append sb (char sep))
	      (recur (.read reader) true))
	  [(.toString sb) :sep])
	
	quote
	(if in-quotes?
	  (let [next-ch (.read reader)]
	    (if (== quote next-ch)
	      (do (.append sb (char quote))
		  (recur (.read reader) true))
	      (recur next-ch false)))
	  (recur (.read reader) true))
	
	lf
	(if in-quotes?
	  (do (.append sb \newline)
	      (recur (.read reader) true))
	  [(.toString sb) :eol])
	
	cr
	(if in-quotes?
	  (do (.append sb \return)
	      (recur (.read reader) true))
	  (let [next-ch (.read reader)]
	    (if (== next-ch lf)
	      [(.toString sb) :eol]
	      (do (.append sb \return)
		  (recur next-ch false)))))
	
	eof
	[(.toString sb) :eof]
	
	;; else
	(do (.append sb (char ch))
	    (recur (.read reader) in-quotes?))))))

(defn- read-record [reader sep quote]
  (loop [record (transient [])]
    (let [[cell sentinel] (read-cell reader sep quote)]
      (case sentinel
	:sep
	(recur (conj! record cell))
	;; else
	[(persistent! (conj! record cell)) sentinel]))))

(defn- read* [reader sep quote]
  (lazy-seq
   (let [[record sentinel] (read-record reader sep quote)]
     (case sentinel
       :eol
       (cons record (read* reader sep quote))
       :eof
       (when-not (= record [""])
	 (cons record nil))))))

(defn read
  "Reads cells from reader using the separator (default \\,) and
  quote (default \\\") characters. Records are separated by either \\n
  or \\r\\n. Returns a lazy sequence of records (vectors) containing
  the cells (strings). The reader is not closed."
  [reader & {:keys [separator quote]
	     :or   {separator \,
		    quote \"}}]
  (read* reader (int separator) (int quote)))

(defn- write-cell [^Writer writer obj sep quote]
  (let [string (str obj)
	must-quote (some #{sep quote \newline} string)]
    (when must-quote (.write writer (int quote)))
    (.write writer (if must-quote
		     (str/escape string
				 {quote (str quote quote)})
		     string))
    (when must-quote (.write writer (int quote)))))

(defn- write-record [^Writer writer record sep quote]
  (loop [record record]
    (when-first [cell record]
      (write-cell writer cell sep quote)
      (when-let [more (next record)]
	(.write writer (int sep))
	(recur more)))))

(defn- write* [^Writer writer records sep quote ^String newline]
  (loop [records records]
    (when-first [record records]
      (write-record writer record sep quote)
      (.write writer newline)
      (recur (next records)))))

(defn write
  "Writes the content of records (a sequence) to writer. Each
  record (s sequence) is separated with newline (either :lf (default)
  or :cr+lf). Each cell (any object) is separated with
  separator (default \\,). Cells are quoted (default \\\") only when
  needed. The writer is not closed."

  [writer records & {:keys [separator quote newline]
		     :or   {separator \,
			    quote \"
			    newline :lf}}]
  (write* writer
	  records
	  separator
	  quote
	  ({:lf "\n" :cr+lf "\r\n"} newline)))


