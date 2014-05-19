(ns moviejure.user_content
  (:use korma.db
        korma.core
        compojure.core
        compojure.handler
        [moviejure.db :as db]
        [moviejure.response :as resp]
        moviejure.user))

(defentity user_content)
(defentity user_comment)

(defn- insert-user-content [user-id content-id favorite]
  (insert user_content (values {:user_id user-id :content_id content-id :favorite favorite :created (db/get-current-date)})))

(defn- insert-user-comment [user-id content-id comment created]
  (insert user_comment (values {:user_id user-id :content_id content-id :comment comment :created created})))

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

(defn- add-comment-response [user content-id comment]
  (let [created (db/get-current-date)]
  (try
   (if-not (insert-user-comment (:id user) content-id comment created)
    (resp/edn {:content-id content-id :status-text "Can't add comment"} 420)
    (resp/edn {:content-id content-id :comment comment :created created :user-name (:name user)}))
  (catch Exception e (resp/edn {:content-id content-id :error-message (.getMessage e)} 420)))))

(defn get-favorites [user-id]
  (select user_content (where {:user_id user-id})))

(defn get-comments [content-id]
  (select user_comment
          (fields :comment :created [:users.name :user-name])
          (join users (= :users.id :user_comment.user_id))
          (where {:content_id content-id})
          (order :created :DESC)))

(defroutes user-content-routes
  (POST "/set_favorite" [content-id favorite :as {{user :user} :session}] (set-favorite-response user content-id favorite))
  (POST "/add_comment" [content-id comment :as {{user :user} :session}] (add-comment-response user content-id comment))
  (GET "/:id/comments" [id] (resp/edn (get-comments (read-string id)))))
