(ns wutchamean.core-test
  (:require [midje.sweet :refer :all]
            [wutchamean.core :refer :all]))

(def grammar
  {:grammar {
             :animal ["dog" "cat" "hunted wumpus" "superb lyrebird of southern australia" "Australian Bear" "Austrian Cat Southern-style"]
             :person ["Bill" "Kris" "Trevor" "Cat Woman" "Dogwoman" "Bird Man" "hunter" "Australian" "Austrian" "Warren Beatty"]}})

#_(fact "strings get split"
        (split-words-in-string-list (-> grammar :grammar :animal)) 
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

(fact "process-grammar"
      (process-grammar (:grammar grammar))
      => {"australia" [["australia" {:class :animal, :index 3, :string "superb lyrebird of southern australia"}]],
          "australian" [["australian" {:class :person, :index 7, :string "Australian"}] 
                        ["australian" {:class :animal, :index 4, :string "Australian Bear"}]],
          "austrian" [["austrian" {:class :person, :index 8, :string "Austrian"}] 
                      ["austrian" {:class :animal, :index 5, :string "Austrian Cat Southern-style"}]],
          "bear" [["bear" {:class :animal, :index 4, :string "Australian Bear"}]],
          "beatty" [["beatty" {:class :person, :index 9, :string "Warren Beatty"}]],
          "bill" [["bill" {:class :person, :index 0, :string "Bill"}]],
          "bird" [["bird" {:class :person, :index 5, :string "Bird Man"}]],
          "cat" [["cat" {:class :person, :index 3, :string "Cat Woman"}] 
                 ["cat" {:class :animal, :index 1, :string "cat"}] 
                 ["cat" {:class :animal, :index 5, :string "Austrian Cat Southern-style"}]],
          "dog" [["dog" {:class :animal, :index 0, :string "dog"}]],
          "dogwoman" [["dogwoman" {:class :person, :index 4, :string "Dogwoman"}]],
          "hunted" [["hunted" {:class :animal, :index 2, :string "hunted wumpus"}]],
          "hunter" [["hunter" {:class :person, :index 6, :string "hunter"}]],
          "kris" [["kris" {:class :person, :index 1, :string "Kris"}]],
          "lyrebird" [["lyrebird" {:class :animal, :index 3, :string "superb lyrebird of southern australia"}]],
          "man" [["man" {:class :person, :index 5, :string "Bird Man"}]],
          "of" [["of" {:class :animal, :index 3, :string "superb lyrebird of southern australia"}]],
          "southern" [["southern" {:class :animal, :index 3, :string "superb lyrebird of southern australia"}] 
                      ["southern" {:class :animal, :index 5, :string "Austrian Cat Southern-style"}]],
          "style" [["style" {:class :animal, :index 5, :string "Austrian Cat Southern-style"}]],
          "superb" [["superb" {:class :animal, :index 3, :string "superb lyrebird of southern australia"}]],
          "trevor" [["trevor" {:class :person, :index 2, :string "Trevor"}]],
          "warren" [["warren" {:class :person, :index 9, :string "Warren Beatty"}]],
          "woman" [["woman" {:class :person, :index 3, :string "Cat Woman"}]],
          "wumpus" [["wumpus" {:class :animal, :index 2, :string "hunted wumpus"}]]}
      
      )
