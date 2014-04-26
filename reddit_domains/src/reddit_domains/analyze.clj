(ns reddit-domains.analyze
  (:require [clojure.java.io :as io])
  (:use [incanter core stats datasets])
  (:import [java.io PushbackReader]))

(defn read-corpus-file
  [corpus-file]
  (let [rdr (-> corpus-file
                io/reader
                PushbackReader.)]
    (take-while
     identity
     (repeatedly #(read rdr)))))
