(defproject moviejure "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.1.6"]]
  :uberjar-name "moviejure-standalone.jar"
  :min-lein-version "2.0.0"
  :ring {:handler moviejure.core/app})
