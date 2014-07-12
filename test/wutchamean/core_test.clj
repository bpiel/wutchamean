(ns wutchamean.core-test
  (:require [midje.sweet :refer :all]
            [wutchamean.core :refer :all]))

(def grammar
  {:grammar {
    :animal ["dog" "cat" "hunted wumpus" "superb lyrebird of southern australia" "Australian Bear" "Austrian Cat Southern-style"]
    :person ["Bill" "Kris" "Trevor" "Cat Woman" "Dogwoman" "Bird Man" "hunter" "Australian" "Austrian" "Warren Beatty"]}})

(fact "strings get split"
  (split-words-in-string (-> grammar :grammar :animal)) 
  => [["dog" {:index 0, :string "dog"}]
	  ["cat" {:index 1, :string "cat"}]
	  ["hunted" {:index 2, :string "hunted wumpus"}]
	  ["wumpus" {:index 2, :string "hunted wumpus"}]
	  ["superb" {:index 3, :string "superb lyrebird of southern australia"}]
	  ["lyrebird" {:index 3, :string "superb lyrebird of southern australia"}]
	  ["of" {:index 3, :string "superb lyrebird of southern australia"}]
	  ["southern" {:index 3, :string "superb lyrebird of southern australia"}]
	  ["australia" {:index 3, :string "superb lyrebird of southern australia"}]
	  ["australian" {:index 4, :string "Australian Bear"}]
	  ["bear" {:index 4, :string "Australian Bear"}]
	  ["austrian" {:index 5, :string "Austrian Cat Southern-style"}]
	  ["cat" {:index 5, :string "Austrian Cat Southern-style"}]
	  ["southern" {:index 5, :string "Austrian Cat Southern-style"}]
	  ["style" {:index 5, :string "Austrian Cat Southern-style"}]])