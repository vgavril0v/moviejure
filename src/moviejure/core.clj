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
       (if (> (count fav) 0) (assoc l :results (merge-favorites res fav)) l))
    l))

(defn- add-info-favorite [info id user]
  (if user
    (assoc info :favorite (is-favorite (:id user) id))
    info
  ))

(defn- get-content-info [id type]
  (if (= type :movie) (movie-info id) (series-info id)))

(defn- get-favorite-content [user-id page]
  (let [fav (get-favorites-paged user-id page 10)]
    (assoc fav :results (map #(merge % (get-content-info (:content_id %) (:type %))) (:results fav)))))


(defn popular-movie-list [page user]
  (resp/edn (add-favorites (popular-movies page) user)))

(defn popular-series-list [page user]
  (resp/edn (add-favorites (popular-series page) user)))

(defn favorite-list [page user]
  (resp/edn (get-favorite-content (:id user) page)))


(defroutes movie-routes
  (GET "/" [] (get-index-page-response))
  (GET "/popular" [page :as {{user :user} :session}] (popular-movie-list page user))
  (GET "/:id" [id :as {{user :user} :session}] (resp/edn (add-info-favorite (movie-info id) (read-string id) user))))

(defroutes series-routes
  (GET "/" [] (get-index-page-response))
  (GET "/popular" [page :as {{user :user} :session}] (popular-series-list page user))
  (GET "/:id" [id :as {{user :user} :session}] (resp/edn (add-info-favorite (series-info id) (read-string id) user))))

(defroutes favorite-routes
  (GET "/" [] (get-index-page-response))
  (GET "/list" [page :as {{user :user} :session}] (favorite-list (read-string page) user))
  )

(defroutes compojure-handler
  (GET "/" [] (get-index-page))
  (context "/movies" [] movie-routes)
  (context "/series" [] series-routes)
  (context "/favorites" [] favorite-routes)
  (context "/user" [] user-routes)
  (context "/user_content" [] user-content-routes)
  (GET "/config" [] (resp/edn (configuration)))
  (GET "/req" request (str request))
  (route/resources "/")
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site
      wrap-edn-params))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
