(ns moviejure.core
  (:require [ring.adapter.jetty :as jetty]))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hi there"})

(def app handler)

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))