(ns client.core
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
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
(em/defsnippet pager site-template "#pager" [page-num total-pages]
               "#current_page" (ef/content (str page-num))
               "#total_pages" (ef/content (str total-pages)))

(em/defsnippet card-closer site-template "#card-closer" [])
(em/defsnippet card site-template "#card" [])

(em/defsnippet movie-item site-template "#movie-item" [{:keys [id title name release_date first_air_date poster_path favorite type]}]
               "#item-title" (ef/content (or title name))
               "#item-date" (ef/content (get-date-year (or release_date first_air_date)))
               "#item-cover" (set-poster-source poster_path (get-poster-size second))
               "#favorite" (client.user_content.favorite-handler id favorite)
               "#card-ref" (ev/listen :click #(load-content-card id type)))

(em/defsnippet first-movie-item site-template "#first-movie-item" [{:keys [id title name poster_path overview release_date first_air_date genres homepage favorite type]}]
               "#item-title" (ef/content (or title name))
               "#item-date"  (ef/content (get-date-year (or release_date first_air_date)))
               "#item-overview" (ef/content overview)
               "#item-cover" (set-poster-source poster_path (get-poster-size second))
               "#item-genres" (ef/content (clojure.string/join ", " (map :name genres)))
               "#item-url" (ef/do-> (ef/content homepage)
                                    (ef/set-attr :href homepage))
               "#favorite" (client.user_content.favorite-handler id favorite)
               "#card-ref" (ev/listen :click #(load-content-card id type)))

(em/defsnippet movie-card site-template "#movie-card" [{:keys [id title name poster_path overview release_date first_air_date genres homepage favorite]}]
               "#item-title" (ef/content (or title name))
               "#item-date"  (ef/content (get-date-year (or release_date first_air_date)))
               "#item-overview" (ef/content overview)
               "#item-cover" (set-poster-source poster_path (get-poster-size second))
               "#item-genres" (ef/content (clojure.string/join ", " (map :name genres)))
               "#item-url" (ef/do-> (ef/content homepage)
                                    (ef/set-attr :href homepage))
               "#favorite" (client.user_content.favorite-handler id favorite))

(em/defsnippet movie-row site-template "#movie-row" [inner-tpl data]
               "#movie-row"  (ef/content (map inner-tpl data)))

(em/defsnippet user-comment site-template "#comment" [{:keys [comment created user-name]}]
               "#comment-date" (ef/content (str (.toLocaleDateString created) " " (.toLocaleTimeString created)))
               "#comment-user-name" (ef/content user-name)
               "#comment-text" (ef/content comment)
               )

(def config (atom {}))

(defn set-config [data] (swap! config merge data))

(defn set-poster-source [src size]
  (ef/set-attr :src (if src (str (:base_url (:images @config)) size src) "/img/noimage.jpg")))

(defn get-poster-size [handler] (handler (:poster_sizes (:images @config))))

(defn get-date-year [date]
  (if date
  (str "(" (subs date  0 4) ")")
   ""))

(defn set-pager [page-num total-pages load-handler]
  (if (< page-num total-pages)
    (ef/at "#next-page" (ev/listen :click #(load-handler (inc page-num))))
    (ef/at "#next-page" (ef/set-style :color "grey"))
    )
  (if (> page-num 1)
    (ef/at "#prev-page" (ev/listen :click #(load-handler (dec page-num))))
    (ef/at "#prev-page" (ef/set-style :color "grey"))))

(defn popular-content-list [data load-handler]
  (let [page (:page data)
        total-pages (:total_pages data)
        res (:results data)
        first-movie (first res)
        next-movies (partition 3 (next res))]
    (ef/at "#inner-content"
           (ef/do->
            (ef/content (movie-row first-movie-item (list first-movie)))
            (ef/append (pager page total-pages))
            (ef/append (map #(movie-row movie-item %) next-movies))
            (ef/append (card-closer))))
  (set-pager page total-pages load-handler)))

(defn popular-movie-list [data]
  (popular-content-list data try-load-popular-movies))

(defn popular-series-list [data]
  (popular-content-list data try-load-popular-series))

(defn show-error [status-text]
  (ef/at "#error-message" (ef/content status-text) )
  (ef/at "#error-div" (ef/set-style :display "block")))

(defn ^:export hide_error [] (ef/at "#error-div" (ef/set-style :display "none")))

(defn ^:export hide_card []
  (ef/at "#card-closer" (ef/set-style :display "none"))
  (ef/at "#card" (ef/set-style :display "none")))

(defn load-content-card [id type]
  (GET (str "/" (if (= type :movie) "movies" "series")  "/" id)
       {:handler show-content-card
        :error-handler error-handler}))

(defn show-content-card [data]
  (let [id (:id data)]
    (ef/at "#card-closer" (ef/set-style :display "block"))
    (ef/at "#card" (ef/do-> (ef/set-style :display "block")
                          (ef/content (movie-card data))))
    (ef/at "#add-comment" (ef/do->
                         (ev/remove-listeners :click)
                         (ev/listen :click #(client.user_content.try_add_comment id))))
    (load-card-comments id)))

(defn show-card-comments [data]
  (ef/at "#user-comments" (ef/content (map user-comment data)))
  )

(defn load-card-comments [id]
  (GET (str "/user_content/" id "/comments")
       {:handler show-card-comments
        :error-handler error-handler}))

(defn error-handler [{:keys [status status-text]}]
  (show-error status-text))

(defn try-load-popular-movies [page]
  (GET (str "/movies/popular?page=" page)
       {:handler popular-movie-list
        :error-handler error-handler}))

(defn try-load-popular-series [page]
  (GET (str "/series/popular?page=" page)
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
          (ef/append (card))
          (ef/append (site-content))
          ))
  (show-login-header))

(defn load-config []
  (GET "/config"
       {:handler set-config
        :error-handler error-handler}))

(defn start-movies []
  (show-header)
  (load-config)
  (try-load-popular-movies 1))

(defn start-series []
  (show-header)
  (load-config)
  (try-load-popular-series 1))

(defroute "/" [] (em/wait-for-load (start-movies)))

(defroute "/movies" [] (em/wait-for-load (start-movies)))

(defroute "/series" [] (em/wait-for-load (start-series)))

(defn get-pathname
  []
  (str (.-pathname (.-location js/document))))


(doto (History.)
  (goog.events/listen EventType/NAVIGATE #(secretary/dispatch! (get-pathname)))
  (.setEnabled true))
