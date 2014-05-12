(ns moviejure.user_content
  (:use korma.db
        korma.core
        compojure.core
        compojure.handler
        [moviejure.db :as db]
        [moviejure.response :as resp]))

(defentity user_content)

(defn- insert-user-content [user-id content-id favorite]
  (insert user_content (values {:user_id user-id :content_id content-id :favorite favorite :created (db/get-current-date)})))

(defn- set-favorite [user-id content-id favorite]
 (if-let [upd (update user_content
   (set-fields {:favorite favorite})
   (where {:content_id content-id :user_id user-id}))]
   upd
   (insert-user-content user-id content-id favorite)))

(defn- set-favorite-response [user content-id favorite]
  (try
   (if-not (set-favorite (:id user) content-id favorite)
    (resp/edn {:favorite favorite :content-id content-id :status-text "Can't set favorite"} 420)
    (resp/edn {:favorite favorite :content-id content-id}))
  (catch Exception e (resp/edn {:favorite favorite :content-id content-id :error-message (.getMessage e)} 420))))

(defn get-favorites [user-id]
  (select user_content (where {:user_id user-id})))

(defroutes user-content-routes
  (POST "/set_favorite" [content-id favorite :as {{user :user} :session}] (set-favorite-response user content-id favorite)))
