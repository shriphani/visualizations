(ns reddit-domains.analyze
  (:require [clojure.java.io :as io])
  (:use [incanter core stats datasets charts])
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

(defn measure-submissions
  []
  (map
   (fn [i]
     (let [corpus-name (str i ".corpus")]
       [(+ 2006 i) (-> corpus-name
                       read-corpus-file
                       count)]))
   (range 9)))

(defn plot-submissions
  []
  (let [submissions-table (measure-submissions)
        years (map first submissions-table)
        subs  (map second submissions-table)]
    ))
