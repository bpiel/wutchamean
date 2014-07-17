(ns wutchamean.core
  (:require [clojure.string :refer [split lower-case]]
            [incanter.stats :refer :all]))


;(group-by first (split-words-in-string (second rule)))

(defn split-words-in-phrase-list
  [phrase-list]
  (apply concat
         (map (fn [tuple] 
                (map-indexed (fn [idx word] 
                               [(lower-case word) {:phrase (second tuple) :index idx}])
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
          (map (fn [[word-match phrase-match]] (hash-map :confidence (calculate-confidence word word-match)
                                                         :matches (map second phrase-match)
                                                         :match word-match))
               (seq processed-grammar))))

(defn tokenize
  "Split string into tokens"
  [processed-grammar string]
  (into {} (map-indexed #(vector % { :position %
                                    :original %2
                                    :matches (match-token processed-grammar %2)})
                        (split string #"[^\d\w']+"))))

(defn get-stub-phrases-from-tokens
  "Assemble tokens into phrases"
  [processed-grammar tokens]
  (apply concat 
         (apply concat 
                (map (fn [[position token]]
                       (map (fn [word-match]
                              (map (fn [phrase-match]
                                     (hash-map :phrase (:phrase phrase-match)
                                               :start-pos (:position token)
                                               :end-pos (:position token)
                                               :phrase-index (:index phrase-match)
                                               :confidence nil
                                               :class (:class phrase-match)
                                               :original (:original token)
                                               :words [(:original token)]))
                                   (:matches word-match)))
                            (:matches token)))       
                     (seq tokens)))))