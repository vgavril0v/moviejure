(ns client.user_content
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn get-favorite-id [id] (str "favorite" id))

(defn get-favorite-id-selector [id] (str "#" (get-favorite-id id)))

(defn favorite-class-handler [favorite]
  (ef/do-> (ef/add-class (if favorite "selected" ""))
           (ef/remove-class (if-not favorite "selected" ""))))

(defn favorite-set [with-card {:keys [content-id content-type favorite]}]
  (ef/at (get-favorite-id-selector content-id) (favorite-handler content-id content-type favorite))
  (if with-card (ef/at "#favorite_card" (favorite-class-handler favorite))))

(defn comment-added [data]
  (ef/at "#user-comments" (ef/prepend (client.core.user-comment data))))

(defn error-handler [data]
  (if-let [resp (:response data)]
    (do
      (client.core.show-error (:error-message resp))
      (favorite-set {:content-id (:content-id resp) :content-type (:content-type resp) :favorite (not (:favorite resp))}))
    (client.core.error-handler data)))

(defn favorite-handler [id content-type favorite]
  (ef/do->
   (ef/set-attr :id (get-favorite-id id))
   (favorite-class-handler favorite)
   (ev/remove-listeners :click)
   (ev/listen :click #(client.user-content.try_set_favorite id content-type (not favorite) false))))

(defn favorite-card-handler [id content-type favorite]
  (ef/do->
   (favorite-class-handler favorite)
   (ev/remove-listeners :click)
   (ev/listen :click #(client.user-content.try_set_favorite id content-type (not favorite) true))))

(defn show-login-message []
  (client.core.show-error "please log in"))

(defn try_set_favorite [content-id content-type favorite with-card]
  (if-not @client.core.logged-in
    (show-login-message)
    (do
      (ef/at (get-favorite-id-selector content-id) (favorite-class-handler favorite))
      (if with-card (ef/at "#favorite_card" (favorite-class-handler favorite)))
      (POST "/user_content/set_favorite"
            {:handler (partial favorite-set with-card)
             :params {:content-id content-id
                      :content-type content-type
                      :favorite favorite}
             :error-handler error-handler}))))

(defn ^:export try_add_comment [content-id]
  (if-not @client.core.logged-in
    (show-login-message)
    (do (POST "/user_content/add_comment"
              {:handler comment-added
               :params {:content-id content-id
                        :comment (ef/from "#user-comment" (ef/read-form-input))}
               :error-handler error-handler})
      (ef/at "#user-comment" (ef/set-form-input "")))))

