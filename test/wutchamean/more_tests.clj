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
  (let [starttime (System/nanoTime)
        output (f)] 
    [ output (/ (- (System/nanoTime) starttime) 1e9)]))

#_(fact "profiling"
      (let [string "change 1st time orders previous"
            [result1 time1] (timer #(string-to-guessed-phrase-sequences processed-grammar  string))
            [processed-grammar2 time2] (timer #(process-grammar (:grammar grammar)))
            [tokens time3] (timer #(tokenize processed-grammar string))
            [assembled-phrases time4] (timer #(assemble-phrases processed-grammar tokens))
            [sorted-phrases time5] (timer (fn [] (sort #(> (:confidence %) (:confidence %2)) assembled-phrases)))
            [phrase-seq time6] (timer #(get-phrase-sequences sorted-phrases))
            [sorted-phrase-seq time7] (timer #(order-by-confidence-phrase-sequences-from-assembled phrase-seq))
            ]
      (println)
      (println "Execution time 1 :" time1)
      (println "Execution time 2 :" time2)
      (println "Execution time 3 :" time3)
      (println "Execution time 4 :" time4)
      (println "Execution time 5 :" time5)
      (println "Execution time 6 :" time6)
      (println "Execution time 7 :" time7)      
      (println)
      (clojure.pprint/write result1)
        )
      1 => 1
      )