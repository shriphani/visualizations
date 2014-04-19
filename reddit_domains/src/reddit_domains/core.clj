(ns reddit-domains.core
  (:require [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.walk :as walk])
  (:use [clojure.pprint :only [pprint]]))

(def years (range 2006 2015))

(def timestamps (map
                 (fn [y] [(int
                          (/ (time-coerce/to-long
                              (time/date-time y 1 1))
                             1000))
                         (int
                          (/ (time-coerce/to-long
                              (time/date-time y 1 2))
                             1000))])
                 years))

(defn create-url
  [jan1 jan2]
  (str "http://api.reddit.com/search.json?q=timestamp:" jan1 ".." jan2 "&syntax=cloudsearch&sort=new"))

(defn fix-and-download
  [low hi collected-data]
  (pprint [:low low :hi hi])
  (let [url  (create-url low hi)
        data (-> url
                 client/get
                 :body
                 json/parse-string
                 walk/keywordize-keys
                 :data
                 :children)
        new-hi (-> data
                   first
                   :data
                   :created
                   int)]
    (if (< low new-hi)
      (recur low new-hi (concat data collected-data))
      (concat data collected-data))))

(defn get-ts-posts
  []
  (map
   (fn [[t1 t2]]
     (let [downloaded (fix-and-download t1 t2 [])
           i (.indexOf timestamps [t1 t2])]
       (with-open [wrtr (io/writer (str i ".corpus"))]
         (binding [*out* wrtr]
           (pprint downloaded)))))
   (take 1 timestamps)))
