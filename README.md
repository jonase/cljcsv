# cljcsv

Cljcsv is a clojure library for reading and writing comma separated
value files. It is licensed under the [Eclipse open source
license](http://www.opensource.org/licenses/eclipse-1.0.php).

## Installation

Download [the jar file](#) and add it to your classpath. Clojure 1.2
is required. I have not added this library to clojars since there
already is [a csv
library](http://github.com/davidsantiago/clojure-csv) for clojure
there.

## Usage

    (require '(org.github.jonase [csv :as csv])
             '(clojure.java [io :as io]))

    (with-open [in-file (io/reader "in-file.csv")]
      (doall
        (csv/read file)))

    (with-open [out-file (io/writer "out-file.csv")]
      (csv/write out-file
                 [["abc" "def"]
                  ["ghi" "jkl"]]))

## Features

### Reading

Cljcsv supports [RFC
4180](http://tools.ietf.org/html/rfc4180). Additionally, it is
possible to choose separator and qoute characters. Reading is fast
and lazy. See `(doc csv/read)` for available options.

### Writing

See `(doc csv/write)` for available options.


