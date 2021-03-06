(ns wutchamean.core
  (:require [clojure.string :refer [split lower-case join]]
            [incanter.stats :refer :all]))


(defn split-phrase-to-words
  [phrase]
  (filter not-empty (split phrase #"[^\w\d']+")))

(defn split-words-in-phrase-list
  [phrase-list]
  (apply concat
         (map (fn [tuple] 
                (map-indexed (fn [idx word] 
                               [(lower-case word) {:phrase (second tuple) :index idx}])
                             (first tuple)))
              (map #(vector (split-phrase-to-words %) %)
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
                                               :confidence nil
                                               :parent-confidence nil
                                               :class (:class phrase-match)
                                               :original (:original token)
                                               :words [(:original token)]))
                                   (:matches word-match)))
                            (:matches token)))       
                     (seq tokens)))))

(defn sort-words [phrase]
  (join " " (sort (split phrase #"[^\w\d']+"))))

(defn calculate-distance [string1 string2]
  (double (/ (- (count string1)
                (damerau-levenshtein-distance (lower-case string1)
                                              (lower-case string2)))
             (count string1))))

(defn calculate-phrase-confidence
  [phrase-map]
  (let [phrase (:phrase phrase-map)
        words (join " " (:words phrase-map))]
    (max (calculate-distance phrase words)
         (* 0.9 (calculate-distance (sort-words phrase)
                                    (sort-words words))))))

(defn expand-one-phrase-map
  [phrase-map tokens direction]
  (if (nil? phrase-map)
    nil
    (if-let [new-phrase-map
             (let [expander (fn [pm the-key key-fn]
                              
                              (if-let [new-word (:original (get tokens
                                                                (-> pm the-key key-fn)))]
                                (-> phrase-map
                                    (update-in [the-key] key-fn)
                                    (update-in [:words] #(if (= the-key :start-pos)
                                                           (into [new-word] %)
                                                           (into % [new-word]))))))]
               (cond         
                 (= direction :left)
                 (expander phrase-map :start-pos dec)
                 
                 (= direction :right)
                 (expander phrase-map :end-pos inc)
                 
                 (= direction :both)      
                 (expand-one-phrase-map (expand-one-phrase-map phrase-map 
                                                               tokens
                                                               :left) 
                                        tokens 
                                        :right)))]
      (assoc new-phrase-map        
        :parent-confidence (:confidence phrase-map)
        :confidence nil
        :original (join " " (:words new-phrase-map))))))

(defn expand-phrase-maps
  [tokens phrase-maps]
  
  (apply concat
         (map (fn [phrase-map] 
                (filter identity
                        (map #(expand-one-phrase-map phrase-map tokens %)
                             [:left :right :both])))
              phrase-maps)))

(defn assemble-phrases-recur
  [tokens phrase-maps last-batch iterations]
  (if (and (<= 0 iterations)
           (not (empty? last-batch)))
    (if-let [new-phrase-maps (->> last-batch
                                  (expand-phrase-maps tokens)
                                  (map (fn [phrase-map] 
                                         (assoc phrase-map 
                                           :confidence (calculate-phrase-confidence phrase-map))))
                                  (filter #(or (-> % :parent-confidence nil?) (> (:confidence %) (* 0.8 (:parent-confidence %)))))
                                  (filter #(< 0 (:confidence %))))]
      (recur tokens 
             (distinct (map #(dissoc % :parent-confidence)
                            (concat phrase-maps new-phrase-maps))) 
             new-phrase-maps 
             (dec iterations))
      phrase-maps)
    phrase-maps))


(defn assemble-phrases
  [processed-grammar tokens]
  (let [stub-phrase-maps (map (fn [phrase-map] 
                                (assoc phrase-map 
                                  :confidence (calculate-phrase-confidence phrase-map)))
                              (get-stub-phrases-from-tokens processed-grammar tokens))]
    (filter #(> (:confidence %) 0.5) 
            (assemble-phrases-recur tokens stub-phrase-maps stub-phrase-maps 4))))

(defn has-conflict [check-phrase phrase-seq]
  (if (empty? phrase-seq)
    false
    (let [first-phrase (first phrase-seq)]
      (if (or (<= (:start-pos check-phrase) (:start-pos first-phrase) (:end-pos check-phrase))
              (<= (:start-pos check-phrase) (:end-pos first-phrase) (:end-pos check-phrase)))
        true
        (recur check-phrase (rest phrase-seq))))))

(defn pick-most-confident-phrases [remaining-phrases chosen-phrases first-skip max-skip-confidence]
  (if (empty? remaining-phrases)
    [chosen-phrases first-skip]
    (let [first-phrase (first remaining-phrases)
          conflict (has-conflict first-phrase chosen-phrases)]
      (if (not conflict)
        (recur (rest remaining-phrases) 
               (cons first-phrase chosen-phrases) 
               first-skip
               max-skip-confidence)
        (recur (rest remaining-phrases) 
               chosen-phrases 
               (if (and (nil? first-skip) 
                        (> max-skip-confidence 
                           (:confidence first-phrase)))
                 first-phrase
                 first-skip)
               max-skip-confidence)))))

(defn get-phrase-sequences-recursive
  [sorted-phrases phrase-seqs first-skip iterations]
  (if (<= 0 iterations)
    (let [[new-chosen new-first-skip] (pick-most-confident-phrases sorted-phrases 
                                                                   (if (nil? first-skip) nil [first-skip])
                                                                   nil 
                                                                   (or (:confidence first-skip) 2))]
      (if new-first-skip
        (recur sorted-phrases 
               (conj phrase-seqs new-chosen)
               new-first-skip
               (dec iterations))
        phrase-seqs))
    phrase-seqs))


(defn get-phrase-sequences
  [sorted-phrases]
  (get-phrase-sequences-recursive sorted-phrases [] nil 10))

(defn calculate-phrase-seq-confidence 
  [phrase-seq string]
  (let [sorted (sort #(< (:start-pos %)(:start-pos %2)) phrase-seq)
        l-string (lower-case string)
        theory-vec (map :phrase sorted)
        
        theory (join " " theory-vec)
        string-sorted (join " " (-> l-string split-phrase-to-words sort))
        theory-sorted (join " " (sort theory-vec))]
    (max (calculate-distance l-string theory)
         (* 0.9 (calculate-distance string-sorted theory-sorted)))))


(defn order-by-confidence-phrase-sequences-from-assembled [assembled-phrases string]
  (sort #(> (:confidence %)(:confidence %2))
        (map #(hash-map :confidence (calculate-phrase-seq-confidence % string)
                        :phrase-seq (sort (fn [ps1 ps2] (< (:start-pos ps1)(:start-pos ps2))) %))
             assembled-phrases)))

(defn phrase-seq-to-guessed-string
    [phrase-seq]
    (join " " (map :phrase phrase-seq)))

(defn string-to-guessed-phrase-sequences
  [processed-grammar string]  
  (order-by-confidence-phrase-sequences-from-assembled
    (get-phrase-sequences
      (sort #(> (:confidence %) (:confidence %2))
            (assemble-phrases processed-grammar
                              (tokenize processed-grammar
                                        string))))
    string))

(defn string-to-guessed-string
  [processed-grammar string]  
  (map #(-> % :phrase-seq phrase-seq-to-guessed-string)
         (string-to-guessed-phrase-sequences processed-grammar string)))
