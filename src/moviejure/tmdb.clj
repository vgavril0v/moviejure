(ns moviejure.tmdb
  (:require [clj-http.client :as client]))


(def base-url "http://api.themoviedb.org/3/")
(def api_key "651800ee65a94493c6ce7af910e69cfe")

(defn- get-url [url] (str base-url url "?api_key=" api_key))

(defn- call-tmdb [url]
 (:body (client/get (get-url url) {:as :json})))

(defn configuration []
  (call-tmdb "configuration"))

(defn popular-movies []
   (call-tmdb "movie/popular"))

(defn popular-series []
   (call-tmdb "tv/popular"))
