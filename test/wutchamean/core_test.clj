(ns wutchamean.core-test
  (:require [midje.sweet :refer :all]
            [wutchamean.core :refer :all]))

(def grammar
  {:grammar {
             :animal ["dog" "cat" "hunted wumpus" "superb lyrebird of southern australia" "Australian Bear" "Austrian Cat Southern-style"]
             :person ["Bill" "Kris" "Trevor" "Cat Woman" "Dogwoman" "Bird Man" "hunter" "Australian" "Austrian" "Warren Beatty"]}})

(fact "strings get split"
      (split-words-in-phrase-list (-> grammar :grammar :animal)) 
      => [["dog" {:index 0, :phrase "dog"}]
          ["cat" {:index 0, :phrase "cat"}]
          ["hunted" {:index 0, :phrase "hunted wumpus"}]
          ["wumpus" {:index 1, :phrase "hunted wumpus"}]
          ["superb" {:index 0, :phrase "superb lyrebird of southern australia"}]
          ["lyrebird" {:index 1, :phrase "superb lyrebird of southern australia"}]
          ["of" {:index 2, :phrase "superb lyrebird of southern australia"}]
          ["southern" {:index 3, :phrase "superb lyrebird of southern australia"}]
          ["australia" {:index 4, :phrase "superb lyrebird of southern australia"}]
          ["australian" {:index 0, :phrase "Australian Bear"}]
          ["bear" {:index 1, :phrase "Australian Bear"}]
          ["austrian" {:index 0, :phrase "Austrian Cat Southern-style"}]
          ["cat" {:index 1, :phrase "Austrian Cat Southern-style"}]
          ["southern" {:index 2, :phrase "Austrian Cat Southern-style"}]
          ["style" {:index 3, :phrase "Austrian Cat Southern-style"}]])

(fact "strings get split -- tricky words"
      (split-words-in-phrase-list ["(Email Summary) - % change 1st time orders previous 7 days"]) 
      => [["email"
           {:index 0,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["summary"
           {:index 1,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["change"
           {:index 2,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["1st"
           {:index 3,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["time"
           {:index 4,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["orders"
           {:index 5,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["previous"
           {:index 6,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["7"
           {:index 7,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]
          ["days"
           {:index 8,
            :phrase
            "(Email Summary) - % change 1st time orders previous 7 days"}]])


(fact "process-grammar"
      (process-grammar (:grammar grammar))
      => {"australia" [["australia"
                        {:class :animal,
                         :index 4,
                         :phrase "superb lyrebird of southern australia"}]],
          "dog" [["dog" {:class :animal, :index 0, :phrase "dog"}]],
          "lyrebird" [["lyrebird"
                       {:class :animal,
                        :index 1,
                        :phrase "superb lyrebird of southern australia"}]],
          "of" [["of"
                 {:class :animal,
                  :index 2,
                  :phrase "superb lyrebird of southern australia"}]],
          "austrian" [["austrian" {:class :person, :index 0, :phrase "Austrian"}]
                      ["austrian"
                       {:class :animal, :index 0, :phrase "Austrian Cat Southern-style"}]],
          "bill" [["bill" {:class :person, :index 0, :phrase "Bill"}]],
          "bird" [["bird" {:class :person, :index 0, :phrase "Bird Man"}]],
          "hunted" [["hunted" {:class :animal, :index 0, :phrase "hunted wumpus"}]],
          "warren" [["warren" {:class :person, :index 0, :phrase "Warren Beatty"}]],
          "superb" [["superb"
                     {:class :animal,
                      :index 0,
                      :phrase "superb lyrebird of southern australia"}]],
          "style" [["style"
                    {:class :animal, :index 3, :phrase "Austrian Cat Southern-style"}]],
          "man" [["man" {:class :person, :index 1, :phrase "Bird Man"}]],
          "bear" [["bear" {:class :animal, :index 1, :phrase "Australian Bear"}]],
          "wumpus" [["wumpus" {:class :animal, :index 1, :phrase "hunted wumpus"}]],
          "dogwoman" [["dogwoman" {:class :person, :index 0, :phrase "Dogwoman"}]],
          "woman" [["woman" {:class :person, :index 1, :phrase "Cat Woman"}]],
          "hunter" [["hunter" {:class :person, :index 0, :phrase "hunter"}]],
          "kris" [["kris" {:class :person, :index 0, :phrase "Kris"}]],
          "australian" [["australian" {:class :person, :index 0, :phrase "Australian"}]
                        ["australian"
                         {:class :animal, :index 0, :phrase "Australian Bear"}]],
          "southern" [["southern"
                       {:class :animal,
                        :index 3,
                        :phrase "superb lyrebird of southern australia"}]
                      ["southern"
                       {:class :animal, :index 2, :phrase "Austrian Cat Southern-style"}]],
          "cat" [["cat" {:class :person, :index 0, :phrase "Cat Woman"}]
                 ["cat" {:class :animal, :index 0, :phrase "cat"}]
                 ["cat"
                  {:class :animal, :index 1, :phrase "Austrian Cat Southern-style"}]],
          "beatty" [["beatty" {:class :person, :index 1, :phrase "Warren Beatty"}]],
          "trevor" [["trevor" {:class :person, :index 0, :phrase "Trevor"}]]})

(fact "match token (hutne)"
      (match-token (process-grammar (:grammar grammar)) "hutne")
      => [{:confidence 0.6666666666666667,
           :match "hunted",
           :matches
           [{:class :animal, :index 0, :phrase "hunted wumpus"}]}
          {:confidence 0.6666666666666667,
           :match "hunter",
           :matches [{:class :person, :index 0, :phrase "hunter"}]}])

(fact "match token (cart)"
      (match-token (process-grammar (:grammar grammar)) "cart")
      => [{:confidence 0.6666666666666667,
           :match "cat",
           :matches
           [{:class :person, :index 0, :phrase "Cat Woman"}
            {:class :animal, :index 0, :phrase "cat"}
            {:class :animal, :index 1, :phrase "Austrian Cat Southern-style"}]}])

(fact "tokenize"
      (tokenize (process-grammar (:grammar grammar)) "Dog cat wumpus hutne")
      => {0
          {:matches
           [{:confidence 1.0,
             :match "dog",
             :matches [{:class :animal, :index 0, :phrase "dog"}]}],
           :original "Dog",
           :position 0},
          1
          {:matches
           [{:confidence 1.0,
             :match "cat",
             :matches
             [{:class :person, :index 0, :phrase "Cat Woman"}
              {:class :animal, :index 0, :phrase "cat"}
              {:class :animal,
               :index 1,
               :phrase "Austrian Cat Southern-style"}]}],
           :original "cat",
           :position 1},
          2
          {:matches
           [{:confidence 1.0,
             :match "wumpus",
             :matches [{:class :animal, :index 1, :phrase "hunted wumpus"}]}],
           :original "wumpus",
           :position 2},
          3
          {:matches
           [{:confidence 0.6666666666666667,
             :match "hunted",
             :matches [{:class :animal, :index 0, :phrase "hunted wumpus"}]}
            {:confidence 0.6666666666666667,
             :match "hunter",
             :matches [{:class :person, :index 0, :phrase "hunter"}]}],
           :original "hutne",
           :position 3}})

(fact "get stub phrases from tokens"
      (let [processed-grammar (process-grammar (:grammar grammar))]
        (get-stub-phrases-from-tokens processed-grammar
                                      (tokenize processed-grammar "Dog cart wompus hutne"))
        => [{:words ["Dog"],
             :end-pos 0,
             :original "Dog",
             :parent-confidence nil,
             :phrase "dog",
             :class :animal,
             :confidence nil,
             :start-pos 0}
            {:words ["cart"],
             :end-pos 1,
             :original "cart",
             :parent-confidence nil,
             :phrase "Cat Woman",
             :class :person,
             :confidence nil,
             :start-pos 1}
            {:words ["cart"],
             :end-pos 1,
             :original "cart",
             :parent-confidence nil,
             :phrase "cat",
             :class :animal,
             :confidence nil,
             :start-pos 1}
            {:words ["cart"],
             :end-pos 1,
             :original "cart",
             :parent-confidence nil,
             :phrase "Austrian Cat Southern-style",
             :class :animal,
             :confidence nil,
             :start-pos 1}
            {:words ["wompus"],
             :end-pos 2,
             :original "wompus",
             :parent-confidence nil,
             :phrase "hunted wumpus",
             :class :animal,
             :confidence nil,
             :start-pos 2}
            {:words ["hutne"],
             :end-pos 3,
             :original "hutne",
             :parent-confidence nil,
             :phrase "hunted wumpus",
             :class :animal,
             :confidence nil,
             :start-pos 3}
            {:words ["hutne"],
             :end-pos 3,
             :original "hutne",
             :parent-confidence nil,
             :phrase "hunter",
             :class :person,
             :confidence nil,
             :start-pos 3}]))

(fact "expand-one-phrase-map"
      (expand-one-phrase-map {:words ["hutne"],
                              :end-pos 3,
                              :original "hutne",
                              :phrase-index 0,
                              :parent-confidence nil,
                              :phrase "hunter",
                              :class :person,
                              :confidence nil,
                              :start-pos 3}
                             (tokenize (process-grammar (:grammar grammar)) "Dog cart wompus hutne")
                             :left) 
      
      => {:words ["wompus" "hutne"],
          :end-pos 3,
          :original "wompus hutne",
          :phrase-index 0,
          :parent-confidence nil,
          :phrase "hunter",
          :class :person,
          :confidence nil,
          :start-pos 2})

(fact "expand-phrase-maps"
      (let [string "Dog cart wompus hutne"
            processed-grammar (process-grammar (:grammar grammar))
            tokens {0
                    {:matches
                     [{:confidence 1.0,
                       :match "dog",
                       :matches [{:class :animal, :index 0, :phrase "dog"}]}],
                     :original "Dog",
                     :position 0},
                    1
                    {:matches
                     [{:confidence 1.0,
                       :match "cat",
                       :matches
                       [{:class :person, :index 0, :phrase "Cat Woman"}
                        {:class :animal, :index 0, :phrase "cat"}
                        {:class :animal,
                         :index 1,
                         :phrase "Austrian Cat Southern-style"}]}],
                     :original "cat",
                     :position 1},
                    2
                    {:matches
                     [{:confidence 1.0,
                       :match "wumpus",
                       :matches [{:class :animal, :index 1, :phrase "hunted wumpus"}]}],
                     :original "wumpus",
                     :position 2},
                    3
                    {:matches
                     [{:confidence 0.6666666666666667,
                       :match "hunted",
                       :matches [{:class :animal, :index 0, :phrase "hunted wumpus"}]}
                      {:confidence 0.6666666666666667,
                       :match "hunter",
                       :matches [{:class :person, :index 0, :phrase "hunter"}]}],
                     :original "hutne",
                     :position 3}}
            stub-phrases [{:words ["Dog"],
                           :end-pos 0,
                           :original "Dog",
                           :parent-confidence nil,
                           :phrase "dog",
                           :class :animal,
                           :confidence nil,
                           :start-pos 0}                          
                          {:words ["cart"],
                           :end-pos 1,
                           :original "cart",
                           :parent-confidence nil,
                           :phrase "Austrian Cat Southern-style",
                           :class :animal,
                           :confidence 0.6,
                           :start-pos 1}                          
                          {:words ["hutne"],
                           :end-pos 3,
                           :original "hutne",
                           :parent-confidence nil,
                           :phrase "hunter",
                           :class :person,
                           :confidence 0.3,
                           :start-pos 3}]]
        
        (expand-phrase-maps tokens
                            stub-phrases))
      => [{:words ["Dog" "cat"],
           :end-pos 1,
           :original "Dog cat",
           :parent-confidence nil,
           :phrase "dog",
           :class :animal,
           :confidence nil,
           :start-pos 0}
          {:words ["Dog" "cart"],
           :end-pos 1,
           :original "Dog cart",
           :parent-confidence 0.6,
           :phrase "Austrian Cat Southern-style",
           :class :animal,
           :confidence nil,
           :start-pos 0}
          {:words ["cart" "wumpus"],
           :end-pos 2,
           :original "cart wumpus",
           :parent-confidence 0.6,
           :phrase "Austrian Cat Southern-style",
           :class :animal,
           :confidence nil,
           :start-pos 1}
          {:words ["Dog" "cart" "wumpus"],
           :end-pos 2,
           :original "Dog cart wumpus",
           :parent-confidence 0.6,
           :phrase "Austrian Cat Southern-style",
           :class :animal,
           :confidence nil,
           :start-pos 0}
          {:words ["wumpus" "hutne"],
           :end-pos 3,
           :original "wumpus hutne",
           :parent-confidence 0.3,
           :phrase "hunter",
           :class :person,
           :confidence nil,
           :start-pos 2}]
      )


(fact "assemble-phrases"
      (let [processed-grammar (process-grammar (:grammar grammar))]
        (assemble-phrases processed-grammar
                          (tokenize processed-grammar "Dog cart wompus hutne")))
      => [{:class :animal,
           :confidence 1.0,
           :end-pos 0,
           :original "Dog",
           :phrase "dog",
           :start-pos 0,
           :words ["Dog"]}
          {:class :animal,
           :confidence 0.6666666666666667,
           :end-pos 1,
           :original "cart",
           :phrase "cat",
           :start-pos 1,
           :words ["cart"]}
          {:class :person,
           :confidence 0.6666666666666667,
           :end-pos 3,
           :original "hutne",
           :phrase "hunter",
           :start-pos 3,
           :words ["hutne"]}
          {:class :person,
           :confidence 0.5555555555555556,
           :end-pos 2,
           :original "cart wompus",
           :phrase "Cat Woman",
           :start-pos 1,
           :words ["cart" "wompus"]}
          {:class :animal,
           :confidence 0.5384615384615385,
           :end-pos 2,
           :original "cart wompus",
           :phrase "hunted wumpus",
           :start-pos 1,
           :words ["cart" "wompus"]}
          {:class :animal,
           :confidence 0.6923076923076923,
           :end-pos 3,
           :original "wompus hutne",
           :phrase "hunted wumpus",
           :start-pos 2,
           :words ["wompus" "hutne"]}] )

(fact "has conflict -- inside"
      (has-conflict {:start-pos 3 :end-pos 10}
                    [{:start-pos 12 :end-pos 14}
                     {:start-pos 5 :end-pos 9}])
      => true)

(fact "has conflict -- left"
      (has-conflict {:start-pos 3 :end-pos 10}
                    [{:start-pos 12 :end-pos 14}
                     {:start-pos 1 :end-pos 4}])
      => true)

(fact "has conflict -- left"
      (has-conflict {:start-pos 3 :end-pos 10}
                    [{:start-pos 12 :end-pos 14}
                     {:start-pos 7 :end-pos 12}])
      => true)

(fact "has conflict -- there is no conflict"
      (has-conflict {:start-pos 3 :end-pos 10}
                    [{:start-pos 12 :end-pos 14}
                     {:start-pos 15 :end-pos 18}])
      => false)


(fact "has conflict -- conflict!"
      (has-conflict { :start-pos 5 :end-pos 6}
                    [{ :start-pos 4 :end-pos 5}])
      => true)


(fact "pick-most-confident-phrases -- conflict"
      (pick-most-confident-phrases [{ :start-pos 1 :end-pos 2 :confidence 1}
                                    { :start-pos 1 :end-pos 4 :confidence 1}
                                    { :start-pos 5 :end-pos 6 :confidence 1}
                                    { :start-pos 4 :end-pos 5 :confidence 1}]
                                   []
                                   nil
                                   2)
      => [[{:end-pos 6, :start-pos 5 :confidence 1} {:end-pos 2, :start-pos 1 :confidence 1}]
          {:end-pos 4, :start-pos 1 :confidence 1}])

(fact "pick-most-confident-phrases -- no conflict"
      (pick-most-confident-phrases [{ :start-pos 1 :end-pos 2 :confidence 1}
                                    { :start-pos 10 :end-pos 12 :confidence 1}
                                    { :start-pos 5 :end-pos 6 :confidence 1}]
                                   []
                                   nil
                                   2)
      => [[{:end-pos 6, :start-pos 5 :confidence 1} {:end-pos 12, :start-pos 10 :confidence 1} {:end-pos 2, :start-pos 1 :confidence 1}]
          nil])

(fact "get-phrase-sequences-recursive"
      (let [sorted-phrases [{:class :animal,
                             :confidence 0.6923076923076923,
                             :end-pos 3,
                             :original "wompus hutne",
                             :phrase "hunted wumpus",
                             :start-pos 2,
                             :words ["wompus" "hutne"]}
                            {:class :animal,
                             :confidence 1.0,
                             :end-pos 0,
                             :original "Dog",
                             :phrase "dog",
                             :start-pos 0,
                             :words ["Dog"]}
                            {:class :animal,
                             :confidence 0.6666666666666667,
                             :end-pos 1,
                             :original "cart",
                             :phrase "cat",
                             :start-pos 1,
                             :words ["cart"]}
                            {:class :person,
                             :confidence 0.6666666666666667,
                             :end-pos 3,
                             :original "hutne",
                             :phrase "hunter",
                             :start-pos 3,
                             :words ["hutne"]}
                            {:class :person,
                             :confidence 0.5555555555555556,
                             :end-pos 2,
                             :original "cart wompus",
                             :phrase "Cat Woman",
                             :start-pos 1,
                             :words ["cart" "wompus"]}
                            {:class :animal,
                             :confidence 0.5384615384615385,
                             :end-pos 2,
                             :original "cart wompus",
                             :phrase "hunted wumpus",
                             :start-pos 1,
                             :words ["cart" "wompus"]}
                            ]
            ]
        (get-phrase-sequences-recursive sorted-phrases [] nil 0))
      => [[{:class :animal,
            :confidence 0.6666666666666667,
            :end-pos 1,
            :original "cart",
            :phrase "cat",
            :start-pos 1,
            :words ["cart"]}
           {:class :animal,
            :confidence 1.0,
            :end-pos 0,
            :original "Dog",
            :phrase "dog",
            :start-pos 0,
            :words ["Dog"]}
           {:class :animal,
            :confidence 0.6923076923076923,
            :end-pos 3,
            :original "wompus hutne",
            :phrase "hunted wumpus",
            :start-pos 2,
            :words ["wompus" "hutne"]}]]
      )


(fact "get-phrase-sequences"
      (let [sorted-phrases [{:class :animal,
                             :confidence 0.6923076923076923,
                             :end-pos 3,
                             :original "wompus hutne",
                             :phrase "hunted wumpus",
                             :start-pos 2,
                             :words ["wompus" "hutne"]}
                            {:class :animal,
                             :confidence 1.0,
                             :end-pos 0,
                             :original "Dog",
                             :phrase "dog",
                             :start-pos 0,
                             :words ["Dog"]}
                            {:class :animal,
                             :confidence 0.6666666666666667,
                             :end-pos 1,
                             :original "cart",
                             :phrase "cat",
                             :start-pos 1,
                             :words ["cart"]}
                            {:class :person,
                             :confidence 0.6666666666666667,
                             :end-pos 3,
                             :original "hutne",
                             :phrase "hunter",
                             :start-pos 3,
                             :words ["hutne"]}
                            {:class :person,
                             :confidence 0.5555555555555556,
                             :end-pos 2,
                             :original "cart wompus",
                             :phrase "Cat Woman",
                             :start-pos 1,
                             :words ["cart" "wompus"]}
                            {:class :animal,
                             :confidence 0.5384615384615385,
                             :end-pos 2,
                             :original "cart wompus",
                             :phrase "hunted wumpus",
                             :start-pos 1,
                             :words ["cart" "wompus"]}
                            ]
            ]
        (get-phrase-sequences sorted-phrases))
      => [[{:class :animal,
            :confidence 0.6666666666666667,
            :end-pos 1,
            :original "cart",
            :phrase "cat",
            :start-pos 1,
            :words ["cart"]}
           {:class :animal,
            :confidence 1.0,
            :end-pos 0,
            :original "Dog",
            :phrase "dog",
            :start-pos 0,
            :words ["Dog"]}
           {:class :animal,
            :confidence 0.6923076923076923,
            :end-pos 3,
            :original "wompus hutne",
            :phrase "hunted wumpus",
            :start-pos 2,
            :words ["wompus" "hutne"]}]
          [{:class :animal,
            :confidence 0.6666666666666667,
            :end-pos 1,
            :original "cart",
            :phrase "cat",
            :start-pos 1,
            :words ["cart"]}
           {:class :animal,
            :confidence 1.0,
            :end-pos 0,
            :original "Dog",
            :phrase "dog",
            :start-pos 0,
            :words ["Dog"]}
           {:class :person,
            :confidence 0.6666666666666667,
            :end-pos 3,
            :original "hutne",
            :phrase "hunter",
            :start-pos 3,
            :words ["hutne"]}]
          [{:class :person,
            :confidence 0.6666666666666667,
            :end-pos 3,
            :original "hutne",
            :phrase "hunter",
            :start-pos 3,
            :words ["hutne"]}
           {:class :animal,
            :confidence 1.0,
            :end-pos 0,
            :original "Dog",
            :phrase "dog",
            :start-pos 0,
            :words ["Dog"]}
           {:class :person,
            :confidence 0.5555555555555556,
            :end-pos 2,
            :original "cart wompus",
            :phrase "Cat Woman",
            :start-pos 1,
            :words ["cart" "wompus"]}]]
      )

(fact "calculate-phrase-seq-confidence"
      (calculate-phrase-seq-confidence  [{:class :animal,
                                          :confidence 0.6666666666666667,
                                          :end-pos 1,
                                          :original "cart",
                                          :phrase "cat",
                                          :start-pos 1,
                                          :words ["cart"]}
                                         {:class :animal,
                                          :confidence 1.0,
                                          :end-pos 0,
                                          :original "Dog",
                                          :phrase "dog",
                                          :start-pos 0,
                                          :words ["Dog"]}
                                         {:class :animal,
                                          :confidence 0.6923076923076923,
                                          :end-pos 3,
                                          :original "wompus hutne",
                                          :phrase "hunted wumpus",
                                          :start-pos 2,
                                          :words ["wompus" "hutne"]}]
                                       "Dog cart wompus hutne")
      => 0.7285714285714286)

(fact "order-by-confidence-phrase-sequences-from-assembled"
      (order-by-confidence-phrase-sequences-from-assembled
        [[{:class :animal,
           :confidence 0.6666666666666667,
           :end-pos 1,
           :original "cart",
           :phrase "cat",
           :start-pos 1,
           :words ["cart"]}
          {:class :animal,
           :confidence 1.0,
           :end-pos 0,
           :original "Dog",
           :phrase "dog",
           :start-pos 0,
           :words ["Dog"]}
          {:class :animal,
           :confidence 0.6923076923076923,
           :end-pos 3,
           :original "wompus hutne",
           :phrase "hunted wumpus",
           :start-pos 2,
           :words ["wompus" "hutne"]}]]
        "Dog cart wompus hutne")
      => [{:confidence 0.7285714285714286,
           :phrase-seq
           [{:class :animal,
             :confidence 1.0,
             :end-pos 0,
             :original "Dog",
             :phrase "dog",
             :start-pos 0,
             :words ["Dog"]}
            {:class :animal,
             :confidence 0.6666666666666667,
             :end-pos 1,
             :original "cart",
             :phrase "cat",
             :start-pos 1,
             :words ["cart"]}
            {:class :animal,
             :confidence 0.6923076923076923,
             :end-pos 3,
             :original "wompus hutne",
             :phrase "hunted wumpus",
             :start-pos 2,
             :words ["wompus" "hutne"]}]}])

(fact "phrase-seq-to-guessed-string"
      (phrase-seq-to-guessed-string [{:class :animal,
                                      :confidence 1.0,
                                      :end-pos 0,
                                      :original "Dog",
                                      :phrase "dog",
                                      :start-pos 0,
                                      :words ["Dog"]}
                                     {:class :animal,
                                      :confidence 0.6666666666666667,
                                      :end-pos 1,
                                      :original "cart",
                                      :phrase "cat",
                                      :start-pos 1,
                                      :words ["cart"]}
                                     {:class :animal,
                                      :confidence 0.6923076923076923,
                                      :end-pos 3,
                                      :original "wompus hutne",
                                      :phrase "hunted wumpus",
                                      :start-pos 2,
                                      :words ["wompus" "hutne"]}]
                                    )
      => "dog cat hunted wumpus")

(fact "string-to-guessed-string"
      (let [processed-grammar (process-grammar (:grammar grammar))]
        (string-to-guessed-string processed-grammar "Dog cart wompus hutne"))
      => ["dog cat hunted wumpus" "dog Cat Woman hunter" "dog cat hunter"])

(fact "string-to-guessed-phrase-sequences"
      (let [processed-grammar (process-grammar (:grammar grammar))]
        (string-to-guessed-phrase-sequences processed-grammar  "Dog cart wompus hutne"))
      => [{:confidence 0.7285714285714286,
           :phrase-seq
           [{:class :animal,
             :confidence 1.0,
             :end-pos 0,
             :original "Dog",
             :phrase "dog",
             :start-pos 0,
             :words ["Dog"]}
            {:class :animal,
             :confidence 0.6666666666666667,
             :end-pos 1,
             :original "cart",
             :phrase "cat",
             :start-pos 1,
             :words ["cart"]}
            {:class :animal,
             :confidence 0.6923076923076923,
             :end-pos 3,
             :original "wompus hutne",
             :phrase "hunted wumpus",
             :start-pos 2,
             :words ["wompus" "hutne"]}]}
          {:confidence 0.7142857142857143,
           :phrase-seq
           [{:class :animal,
             :confidence 1.0,
             :end-pos 0,
             :original "Dog",
             :phrase "dog",
             :start-pos 0,
             :words ["Dog"]}
            {:class :person,
             :confidence 0.5555555555555556,
             :end-pos 2,
             :original "cart wompus",
             :phrase "Cat Woman",
             :start-pos 1,
             :words ["cart" "wompus"]}
            {:class :person,
             :confidence 0.6666666666666667,
             :end-pos 3,
             :original "hutne",
             :phrase "hunter",
             :start-pos 3,
             :words ["hutne"]}]}
          {:confidence 0.5238095238095238,
           :phrase-seq
           [{:class :animal,
             :confidence 1.0,
             :end-pos 0,
             :original "Dog",
             :phrase "dog",
             :start-pos 0,
             :words ["Dog"]}
            {:class :animal,
             :confidence 0.6666666666666667,
             :end-pos 1,
             :original "cart",
             :phrase "cat",
             :start-pos 1,
             :words ["cart"]}
            {:class :person,
             :confidence 0.6666666666666667,
             :end-pos 3,
             :original "hutne",
             :phrase "hunter",
             :start-pos 3,
             :words ["hutne"]}]}])