(defproject wutchamean "0.1.0-SNAPSHOT"
  :description "A fuzzy parser for short natural-language queries with narrow vocabulary"
  :url ""
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
