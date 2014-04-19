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
  [[jan1 jan2]]
  (str "http://api.reddit.com/search.json?q=timestamp:" jan1 ".." jan2 "&syntax=cloudsearch"))

(defn paginate-url
  [url next-id]
  (str url "&after=" next-id))

(defn download-pages
  [url collected]
  (let [body (-> url
                 client/get
                 :body
                 json/parse-string
                 walk/keywordize-keys)
        next (-> body
                 :data
                 :after)]
    (if (nil? next)
      (concat collected (-> body :data :children))
      (do
        (Thread/sleep 2000)
        (recur (paginate-url url next)
               (concat collected (-> body :data :children)))))))

(defn fetch-submissions
  []
  (doseq [ts timestamps]
    (let [url  (create-url ts)
          corpus (download-pages url [])]
      (with-open [wrtr (io/writer (str (.indexOf timestamps ts) ".corpus"))]
        (binding [*out* wrtr]
          (pprint corpus))))))
