(ns moviejure.tmdb
  (:require [clj-http.client :as client])
  (:use carica.core))


(def base-url "http://api.themoviedb.org/3/")
(def api_key (config :api-key))

(defn- get-url [url] (str base-url url (if (>= (.indexOf url "?") 0) "&" "?") "api_key=" api_key))

(defn- call-tmdb [url]
 (:body (client/get (get-url url) {:as :json})))

(defn configuration []
  (call-tmdb "configuration"))


(defn movie-info [id]
   (call-tmdb (str "movie/" id)))

(defn series-info [id]
   (call-tmdb (str "tv/" id)))

(defn- prepare-list [l info-handler]
  (let [res (:results l)]
   (assoc l :results (conj (next res) (info-handler (:id (first res)))))))

(defn- pager-url [url page]
  (if page (str url "?page=" page) url))

(defn popular-movies [page]
   (prepare-list (call-tmdb (pager-url "movie/popular" page)) movie-info))

(defn popular-series [page]
   (prepare-list (call-tmdb (pager-url "tv/popular" page)) series-info))








