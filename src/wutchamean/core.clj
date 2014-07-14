(ns wutchamean.core
  (:require [clojure.string :refer [split lower-case]]))


;(group-by first (split-words-in-string (second rule)))

(defn split-words-in-string-list
  [word-list]
  (apply concat
         (map (fn [tuple] 
                (map (fn [word] 
                       [(lower-case word) {:string (second tuple) :index (nth tuple 2)}])
                     (first tuple)))
              (map-indexed #(vector (split %2 #"[^\w\d']+") %2 %)
                           word-list))))

(defn process-grammar
  [grammar]
  (group-by first
            (apply concat (map (fn [rule]
                                 (map (fn [word-pair]
                                        (assoc-in word-pair [1 :class] (first rule)))
                                      (split-words-in-string-list (second rule))))
                               (seq grammar)))))


(defn match-token
  "Match a word to a set of tokens"
  [processed-grammar word]
  (get processed-grammar (lower-case word)))

(defn tokenize
  "Split string into tokens"
  [processed-grammar string]
  (group-by :position
            (map-indexed #(hash-map :position %
                                    :original %2
                                    :matches (match-token processed-grammar %2))
                         (split string #"[^\d\w']+"))))