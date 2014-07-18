(ns wutchamean.more-tests
  (:require [midje.sweet :refer :all]
            [wutchamean.core :refer :all]))

(def grammar 
  {:grammar
   {:figure
    ["(Email Summary) - % change 1st time orders previous 7 days"
     "(Email Summary) - % change in AOV previous 7 days"
     "(Email Summary) - % change in new users previous 7 days"
     "(Email Summary) - % change in orders previous 7 days"
     "(Email Summary) - % change in revenue previous 7 days"
     "(Email Summary) - 1st time orders past 7 days"
     "(Email Summary) - AOV past 7 days"
     "(Email Summary) - new users past 7 days"
     "(Email Summary) - new users yesterday"
     "(Email Summary) - orders past 7 days"
     "(Email Summary) - orders yesterday"
     "(Email Summary) - revenue past 7 days"
     "(Email Summary) - revenue yesterday"
     "(Email Summary) - top 10 items sold yesterday"
     "1st time revenue"
     "1st time v. repeat orders"
     "1st time v. repeat orders by day"
     "1st time v. repeat revenue"
     "All time average lifetime revenue"
     "All time average lifetime revenue - buyers only"],
    :metric
    ["Average 30 day lifetime revenue"
     "Average 365 day lifetime revenue"
     "Average 60 day lifetime revenue"
     "Average 90 day lifetime revenue"
     "Average lifetime revenue"
     "Average order value"
     "Average time between orders"
     "Average time to first order"
     "Customers-id-count"
     "Distinct buyers"
     "Items sold"
     "New users"
     "Number of orders"
     "Orders-id-count"
     "Revenue"
     "Revenue by item"],
    :dashboard
    ["test"
     "test2"
     "Recent activity"
     "Frequency and repeats"
     "Cohort analysis"
     "Revenue and orders"
     "Users"
     "User lists"
     "Customer value"
     "Products"
     "Email summary"
     "Executive overview"]}})

(def processed-grammar (process-grammar (:grammar grammar)))

(defn timer [f] 
  (let [starttime (System/nanoTime)] 
    (f)
    (/ (- (System/nanoTime) starttime) 1e9))) 

(fact "string-to-guessed-phrase-sequences"
      
      (println)
      (println "Execution time:" (timer #(string-to-guessed-phrase-sequences processed-grammar  "change 1st time orders previous 7 days")))
      (println)
      
      1 => 1
      )