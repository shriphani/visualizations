(ns wikipedia-trace.core
  "Plotting the wikipedia trace for a single day"
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-http.client :as client]))

(defn wikipedia-trace-lines
  [filename]
  (-> filename
      io/reader
      line-seq))

(defn process-wikipedia-line
  [a-line]
  (let [[id-s epoch-s url-s]
        (string/split a-line #"\s+")]
    (let [num-id (Integer/parseInt id-s)
          epoch  (quot
                  (Long/parseLong
                   (string/replace epoch-s #"\." ""))
                  1000)]
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
  [filename out-file]
  (with-open [wrtr (io/writer out-file)]
    (binding [*out* wrtr]
     (let [hits-per-ms (process-trace filename)]
       (doseq [[epoch n] hits-per-ms]
         (println
          (format "%d,%d" epoch n)))))))

(defn download-files
  []
  (let [filenames (wikipedia-trace-lines "filename.txt")]
    (doseq [f filenames]
      (let [bd (:body (client/get (str "http://www.wikibench.eu/wiki/2007-09/" f)))]
        (spit f bd)))))

(def start-ts 1190160000)
(def end-ts 1190246400)

(defn trim-data
  [out-file]
  (let [files (map
               #(.getName %)
               (filter
                #(re-find
                  #"wiki"
                  (.getName %))
                (file-seq
                 (java.io.File. "."))))]
    (with-open [wrtr (io/writer out-file)]
      (binding [*out* wrtr]
       (doseq [f files]
         (let [lines (wikipedia-trace-lines f)]
           (doseq [[i e u] (map process-wikipedia-line lines)]
             (when (and (<= start-ts e)
                        (>= end-ts e))
               (println (str e "," 1))))))))))
