(ns client.user_content
  (:require [enfocus.core :as ef]
            [enfocus.events :as ev]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn get-favorite-id [id] (str "favorite" id))

(defn get-favorite-id-selector [id] (str "#" (get-favorite-id id)))

(defn favorite-class-handler [id favorite]
  (ef/do-> (ef/add-class (if favorite "selected" ""))
           (ef/remove-class (if-not favorite "selected" ""))))

(defn favorite-set [{:keys [content-id favorite]}]
    (ef/at (get-favorite-id-selector content-id) (favorite-handler content-id favorite)))

(defn error-handler [data]
  (if-let [resp (:response data)]
    (do
      (client.core.show-error (:error-message resp))
      (favorite-set {:content-id (:content-id resp) :favorite (not (:favorite resp))}))
    (client.core.error-handler data)))

(defn favorite-handler [id favorite]
  (ef/do->
                            (ef/set-attr :id (get-favorite-id id))
                            (favorite-class-handler id favorite)
                            (ev/remove-listeners :click)
                            (ev/listen :click #(client.user-content.try_set_favorite id (not favorite)))))



(defn ^:export try_set_favorite [content-id favorite]
  (ef/at (get-favorite-id-selector content-id) (favorite-class-handler content-id favorite))
  (POST "/user_content/set_favorite"
        {:handler favorite-set
         :params {:content-id content-id
                  :favorite favorite}
         :error-handler error-handler}))
