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
   moviejure.user_content
   [moviejure.response :as resp]))

(defn- get-index-page [] (slurp (io/resource "public/html/index.html")))

(defn- get-index-page-response []
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body (get-index-page)})

(defn- get-favorite [id favorites]
  (let [fav (first (filter #(= id (:content_id %)) favorites))]
    (or (:favorite fav) false)))

(defn- merge-favorites [results favorites]
  (map #(assoc % :favorite (get-favorite (:id %) favorites)) results))

(defn- add-favorites [l user]
  (if user
    (let [res (:results l)
          fav (get-favorites (:id user))]
       (if true (assoc l :results (merge-favorites res fav)) l))
    l))

;(some #(constantly true) fav)
(defn popular-movie-list [page user]
  (resp/edn (add-favorites (popular-movies page) user)))

(defn popular-series-list [page user]
  (resp/edn (add-favorites (popular-series page) user)))

(defroutes movie-routes
  (GET "/" [] (get-index-page-response))
  (GET "/popular" [page :as {{user :user} :session}] (popular-movie-list page user)))

(defroutes series-routes
  (GET "/" [] (get-index-page-response))
  (GET "/popular" [page :as {{user :user} :session}] (popular-series-list page user)))

(defroutes compojure-handler
  (GET "/" [] (get-index-page))
  (context "/movies" [] movie-routes)
  (context "/series" [] series-routes)
  (context "/user" [] user-routes)
  (context "/user_content" [] user-content-routes)
  (GET "/req" request (str request))
  (route/resources "/")
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site
      wrap-edn-params))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
