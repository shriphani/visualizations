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
                 (fn [y]
                   (let [jan1 (int
                               (/ (time-coerce/to-long
                                   (time/date-time y 1 1))
                                  1000))

                         jan2 (int
                               (/ (time-coerce/to-long
                                   (time/date-time y 1 2))
                                  1000))

                         dec31 (- jan1 60000)]
                    [dec31 jan1 jan2]))
                 years))

(defn create-url
  [low high]
  (str "http://api.reddit.com/search.json?q=timestamp:" low ".." high "&syntax=cloudsearch&sort=new"))

(defn create-r-all-url
  [s]
  (format "http://www.reddit.com/r/all/new.json?count=100&before=%s" s))

(defn try-download-times
  ([x] (try-download-times x 10))

  ([x n]
     (if (zero? n)
       nil
       (try (client/get x)
            (catch Exception e (do (Thread/sleep 1000)
                                   (try-download-times x (dec n))))))))

(defn fix-and-download
  [low hi collected-data]
  (pprint [:low low :hi hi])
  (flush)
  (Thread/sleep 2000)
  (let [url  (create-url low hi)
        data (-> url
                 client/get
                 :body
                 json/parse-string
                 walk/keywordize-keys
                 :data
                 :children)
        new-hi (try
                (-> data
                    first
                    :data
                    :created
                    int
                    dec)
                (catch Exception e nil))]
    (if (and  new-hi (< low new-hi))
      (recur low new-hi (concat data collected-data))
      (concat data collected-data))))

(defn starting-point
  [dec31-ts jan1-ts]
  (let [url (create-url dec31-ts jan1-ts)
        data (-> url
                 client/get
                 :body
                 json/parse-string
                 walk/keywordize-keys
                 :data
                 :children
                 first)]))

(defn get-starting-point
  [dec31 jan1]
  (let [url (str (create-url dec31 jan1)
                 "&limit=1")]
    (-> url
        try-download-times
        :body
        json/parse-string
        walk/keywordize-keys
        :data
        :children
        first
        :data
        :name)))

(defn download-till-end
  [starting-pt jan2 writer]
  (let [url (create-r-all-url starting-pt)
        data (-> url
                 try-download-times
                 :body
                 json/parse-string
                 walk/keywordize-keys
                 :data
                 :children)
        next-starting-pt (-> data
                             last
                             :data
                             :name)
        ts (-> data
               last
               :data
               :created
               int)]
    (do (binding [*out* writer]
          (doall (map pprint data))
          (flush)))
    (when (< ts jan2)
      (do (Thread/sleep 2000)
          (recur next-starting-pt
                 jan2
                 writer)))))

(defn build-dataset
  []
  (doseq [[dec31 jan1 jan2] (drop 4 timestamps)]
    (Thread/sleep 2000)
    (let [starting-pt (get-starting-point dec31 jan1)
          corpus-name (str (.indexOf timestamps [dec31 jan1 jan2]) ".corpus")
          writer (io/writer corpus-name)]
      (println jan1 jan2 corpus-name starting-pt)
      (download-till-end starting-pt jan2 writer)
      (.close writer))))
