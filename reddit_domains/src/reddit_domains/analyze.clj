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
     (repeatedly #(try (read rdr)
                       (catch Exception e nil))))))

(defn measure-subreddits
  [corpus-file]
  (reduce
   (fn [acc x]
     (merge-with +' acc {(-> x
                             :data
                             :subreddit)
                         1}))
   {}
   (read-corpus-file corpus-file)))

(defn measure-domains
  [corpus-file]
  (reduce
   (fn [acc x]
     (merge-with +' acc {(-> x
                             :data
                             :domain)
                         1}))
   {}
   (read-corpus-file corpus-file)))

