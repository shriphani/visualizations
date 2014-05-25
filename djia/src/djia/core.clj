(ns djia.core
  (:require [clj-http.client :as client]
            [net.cgrand.enlive-html :as html]))

(def wikipedia-djia-url "http://en.wikipedia.org/wiki/Historical_components_of_the_Dow_Jones_Industrial_Average")

(defn download-tables
  "Downloads tables and spits out nice things"
  []
  (let [body (-> wikipedia-djia-url
                 client/get
                 :body)
        processed-body (-> body
                           java.io.StringReader.
                           html/html-resource)
        dates  (filter
                (fn [t]
                  (re-find #"\d+" t))
                (map
                 html/text
                 (html/select processed-body
                              [:span.mw-headline])))
        tables (map
                (fn [table]
                  (map
                   html/text
                   (html/select table [:td])))
                (html/select processed-body
                             [:table.wikitable]))]
    (map vector dates tables)))
