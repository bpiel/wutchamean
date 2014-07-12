(ns wutchamean.core
  (:require [clojure.string :refer [split lower-case]]))


;(group-by first (split-words-in-string (second rule)))

(defn process-grammar
  [grammar]
  (group-by first
            (apply concat (map (fn [rule]
                                 (map (fn [word-pair]
                                        (assoc-in word-pair [1 :class] (first rule)))
                                      (split-words-in-string-list (second rule))))
                               (seq grammar)))))



(defn split-words-in-string-list
  [word-list]
  (apply concat
         (map (fn [tuple] 
                (map (fn [word] 
                       [(lower-case word) {:string (second tuple) :index (nth tuple 2)}])
                     (first tuple)))
              (map-indexed #(vector (split %2 #"[^\w\d']+") %2 %)
                           word-list))))

(defn match-tokens
  [grammar word]
  (map first 
       (filter #(some #{word} (last %)) 
               (seq grammar))))

(defn tokenize
  "Split string into tokens"
  [grammar string]
  (map (partial match-tokens grammar)
       (split string #"[^\d\w']+")))