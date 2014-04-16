(ns arms-sale-visualization.core
  (:require [clojure.java.io :as io])
  (:use [incanter core io stats charts]))

(def report-2013 "trade_register_report.csv")
(def supplier-receiver-data "supplier_receiver_data.csv")
(def supplier-receiver-matrix "supplier_receiver_matrix.csv")

(defn prepare-suppl-recv-data
  []
  (save
   (sel (read-dataset report-2013 :header true)
        :cols [:Supplier :Recipient]
        :filter (fn [x] (and
                        (not= (nth x 1)
                              "Unknown country")
                        (not= (nth x 0)
                              "Unknown country"))))
   supplier-receiver-data))

(defn prepare-supplier-recv-matrix
  []
  (with-open [wrtr (io/writer supplier-receiver-matrix)]
    (binding [*out* wrtr]
     (let [data (:rows (read-dataset supplier-receiver-data
                                     :header
                                     true))
           reduced (reduce
                    (fn [acc x]
                      (merge-with + acc {x 1}))
                    {}
                    data)]
       (println "Recipient,Supplier,Orders")
       (doseq [[{r :Recipient s :Supplier} n] reduced]
         (println (str r "," s "," n)))))))

