(defproject moviejure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [ring/ring-codec "1.0.0"]
                 [cheshire "5.3.1"]
                 [clj-http "0.9.1"]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]
                 [fogus/ring-edn "0.2.0"]
                 [korma "0.3.1"]
                 [postgresql/postgresql "9.3-1101.jdbc4"]
                 ]
  :uberjar-name "moviejure-standalone.jar"
  :min-lein-version "2.0.0"
  :ring {:handler moviejure.core/app})
