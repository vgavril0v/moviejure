(ns client.core
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]]
            [goog.events :as events]
            [secretary.core :as secretary :include-macros true :refer [defroute]])
  (:require-macros [enfocus.macros :as em])
  (:import goog.History goog.History.EventType))

(def site-template "html/site.html")

(em/defsnippet site-header site-template ".site-header" [])
(em/defsnippet login-header site-template "#login-header" [])
(em/defsnippet login-refs site-template "#login-refs" [])
(em/defsnippet logout-refs site-template "#logout-refs" [])
(em/defsnippet site-content site-template "#content" [])

(em/defsnippet movie-item site-template "#movie-item" [{:keys [id title poster_path]}]
               "#item-title" (ef/content title)
               "#item-cover" (ef/set-attr :src (str "http://image.tmdb.org/t/p/w92/" poster_path)))

(em/defsnippet series-item site-template "#movie-item" [{:keys [id name poster_path]}]
               "#item-title" (ef/content name)
               "#item-cover" (ef/set-attr :src (str "http://image.tmdb.org/t/p/w92/" poster_path)))

(defn popular-movie-list [data]
  (ef/at "#inner-content" (ef/content (map movie-item (:results data)))))

(defn popular-series-list [data]
  (ef/at "#inner-content" (ef/content (map series-item (:results data)))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "Something bad happened: " status " " status-text)))

(defn try-load-popular-movies []
  (GET "/movies/popular"
       {:handler popular-movie-list
        :error-handler error-handler}))

(defn try-load-popular-series []
  (GET "/series/popular"
       {:handler popular-series-list
        :error-handler error-handler}))

(defn populate-login-header [data]
  (if data
    (client.login.user-logged-in data)
    (client.login.user-logged-out)))

(defn show-login-header []
  (GET "/user/loggedin"
       {:handler populate-login-header
        :error-handler error-handler}))

(defn show-header []
  (ef/at ".container"
         (ef/do->
          (ef/content (login-header))
          (ef/append (site-header))
          (ef/append (site-content))))
  (show-login-header))

(defn start-movies []
  (show-header)
  (try-load-popular-movies))

(defn start-series []
  (show-header)
  (try-load-popular-series))

(defroute "/" [] (em/wait-for-load (start-movies)))

(defroute "/movies" [] (em/wait-for-load (start-movies)))

(defroute "/series" [] (em/wait-for-load (start-series)))

(defn get-pathname
  []
  (str (.-pathname (.-location js/document))))


(doto (History.)
(goog.events/listen EventType/NAVIGATE #(secretary/dispatch! (get-pathname)))
(.setEnabled true))
