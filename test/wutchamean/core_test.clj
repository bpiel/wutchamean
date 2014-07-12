(ns wutchamean.core-test
  (:require [midje.sweet :refer :all]
            [wutchamean.core :refer :all]))

(def grammar
  {:grammar {
    :animal ["dog" "cat" "hunted wumpus" "superb lyrebird of southern australia" "Australian Bear" "Austrian Cat Southern-style"]
    :person ["Bill" "Kris" "Trevor" "Cat Woman" "Dogwoman" "Bird Man" "hunter" "Australian" "Austrian" "Warren Beatty"]}})

(fact "strings get split"
  (split-words-in-string (-> grammar :grammar :animal)) 
  => 1)
