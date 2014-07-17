(ns wutchamean.core
  (:require [clojure.string :refer [split lower-case join]]
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
                                               :parent-confidence nil
                                               :class (:class phrase-match)
                                               :original (:original token)
                                               :words [(:original token)]))
                                   (:matches word-match)))
                            (:matches token)))       
                     (seq tokens)))))

(defn calculate-phrase-confidence
  [phrase-map]
  (let [phrase-count (:phrase phrase-map)]
    (double (/ (- (count phrase-count) 
                  (damerau-levenshtein-distance (lower-case phrase-count)
                                                (lower-case (join " " (:words phrase-map))))
                  (count phrase-count))))))

(defn expand-one-phrase-map
  [tokens phrase-map direction]
  (if (nil? phrase-map)
    nil
    (assoc 
      (let [expander (fn [pm the-key key-fn]
                       (if-let [new-word (:original (get tokens 
                                                         (-> pm
                                                             the-key
                                                             key-fn)))]
                         (-> phrase-map
                             (update-in [the-key] key-fn)
                             (update-in [:words] #(cons new-word %)))
                         nil)
                       )]
        (cond         
          (= direction :left)
          (expander phrase-map :start-pos dec)
          
          (= direction :right)
          (expander phrase-map :end-pos inc)
          
          (= direction :both)      
          (-> phrase-map
              (expander :start-pos dec)
              (expander :end-pos inc))))
      
      :parent-confidence (:confidence phrase-map)
      :confidence nil)))

(defn expand-phrase-maps
  [tokens phrase-maps]
  (apply concat
         (map (fn [phrase-map] 
                (filter identity
                        (map #(expand-one-phrase-map phrase-map tokens %)
                             [:left :right :both])))
                        phrase-maps)))

(defn assemble-phrases-recur
  [tokens phrase-maps]
  (if-let [new-phrase-maps (->> phrase-maps
                                (expand-phrase-maps tokens)
                                (map #(update-in % :confidence (calculate-phrase-confidence %)))
                                (filter #(< (:confidence %) (* 0.9 (:parent-confidence %)))))]
    ;(recur tokens (concat phrase-maps new-phrase-maps))
    [1]
    phrase-maps))

(defn assemble-phrases
  [processed-grammar tokens]
  (let [stub-phrase-maps (get-stub-phrases-from-tokens processed-grammar tokens)]
    (filter #(< (:confidence %) 0.5) 
            (assemble-phrases-recur stub-phrase-maps tokens))))
