(ns wutchamean.core
  (:require [clojure.string :refer [split lower-case]]
            [incanter.stats :refer :all]))


(defn split-words-in-phrase-list
  [phrase-list]
  (apply concat
         (map (fn [tuple] 
                (map-indexed (fn [idx word] 
                       [(lower-case word) {:string (second tuple) :index idx}])
                     (first tuple)))
              (map #(vector (split % #"[^\w\d']+") %)
                           phrase-list))))

(defn process-grammar
  [grammar]
  (group-by first
            (apply concat (map (fn [rule]
                                 (map (fn [word-pair]
                                        (assoc-in word-pair [1 :class] (first rule)))
                                      (split-words-in-phrase-list (second rule))))
                               (seq grammar)))))

(defn calculate-confidence [word match]
  (double (/ (- (count match) 
               (damerau-levenshtein-distance (lower-case word) 
                                             match))
            (count match))))

(defn match-token
  "Match a word to a set of tokens"
  [processed-grammar word]
  (filter #(>= (:confidence %) 0.5)
          (map #(hash-map :confidence (calculate-confidence word (first %))
                          :matches (second %)
                          :match (first %))
               (seq processed-grammar))))

(defn tokenize
  "Split string into tokens"
  [processed-grammar string]
  (group-by :position
            (map-indexed #(hash-map :position %
                                    :original %2
                                    :matches (match-token processed-grammar %2))
                         (split string #"[^\d\w']+"))))
