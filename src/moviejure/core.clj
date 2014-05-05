(ns moviejure.core
  (:require [ring.adapter.jetty :as jetty]
            [cheshire.core :as json]
            [ring.util.codec :as codec]
            [compojure.route :as route]
            [clojure.java.io :as io]
            )
  (:use
   compojure.core
   compojure.handler
   carica.core
   ring.middleware.edn
   moviejure.tmdb
   moviejure.user
   [moviejure.response :as resp]))

(defn- get-index-page [] (slurp (io/resource "public/html/index.html")))

(defn- get-index-page-response []
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body (get-index-page)})

(defn popular-movie-list []
  (resp/edn (popular-movies)))

(defn popular-series-list []
  (resp/edn (popular-series)))

(defroutes movie-routes
  (GET "/" [] (get-index-page-response))
  (GET "/popular" [] (popular-movie-list)))

(defroutes series-routes
  (GET "/" [] (get-index-page-response))
  (GET "/popular" [] (popular-series-list)))

(defroutes compojure-handler
  (GET "/" [] (get-index-page))
  (context "/movies" [] movie-routes)
  (context "/series" [] series-routes)
  (context "/user" [] user-routes)
  (GET "/req" request (str request))
  (route/resources "/")
  (route/files "/" {:root (config :external-resources)})
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site
      wrap-edn-params))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
