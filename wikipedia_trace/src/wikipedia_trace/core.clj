(ns wikipedia-trace.core
  "Plotting the wikipedia trace for a single day"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn wikipedia-trace-lines
  [filename]
  (-> filename
      io/reader
      line-seq))

(defn process-wikipedia-line
  [a-line]
  (let [[id-s epoch-s url-s]
        (string/split a-line "\s+")]
    (let [num-id (int id-s)
          epoch  (Double/parseDouble epoch-s)]
      [num-id epoch url-s])))

(defn process-trace
  [filename]
  (let [lines (wikipedia-trace-lines filename)]
    (sort-by
     first
     (reduce
      (fn [acc [id epoch url-s]]
        (merge-with +' acc {epoch 1}))
      {}
      (map process-wikipedia-line lines)))))

(defn write-to-file
  [filename]
  (let [hits-per-ms (process-trace filename)]
    (map
     (fn [[epoch n]]
       (println
        (format "%f,%d" epoch n)))
     hits-per-ms)))
