# cljcsv

Cljcsv is a clojure library for reading and writing comma separated
value (csv) files. It is licensed under the [Eclipse open source
license](http://www.opensource.org/licenses/eclipse-1.0.php). 

The library has been tested on Clojure version 1.2, 1.2.1 and
1.3-alpha8.

If you find bugs or has a feature request please open a ticket on the
github issue tracker.

## Installation

### Using maven

Add 

    <dependency>
      <groupId>cljcsv</groupId>
      <artifactId>cljcsv</artifactId>
      <version>1.2.1</version>
    </dependency>

to your `pom.xml` file.

### Using leiningen

Add `[cljcsv "1.2.1"]` as a dependency to your `project.clj`.

## Usage

    (require '(com.github.jonase [csv :as csv])
             '(clojure.java [io :as io]))

    (with-open [in-file (io/reader "in-file.csv")]
      (doall
        (csv/read in-file)))

    (with-open [out-file (io/writer "out-file.csv")]
      (csv/write out-file
                 [["abc" "def"]
                  ["ghi" "jkl"]]))

## Features

### Reading

cljcsv supports [RFC
4180](http://tools.ietf.org/html/rfc4180). Additionally, it is
possible to choose separator and qoute characters. Reading is *fast*
and *lazy*. See `(doc csv/read)` for available options.

### Writing

See `(doc csv/write)` for available options.


