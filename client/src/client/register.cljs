(ns client.register
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn ^:export show_form []
  (ef/at "#register-form" (ef/set-attr :style "display:block;")))

(defn ^:export close_form []
  (ef/at "#register-form" (ef/set-attr :style "display:none;")))

(defn user-saved [response]
  (close_form))

(defn ^:export try-create-user []
    (.log js/console (ef/from "#reg-user-name" (ef/read-form-input)))
  (.log js/console (ef/from "#reg-user-login" (ef/read-form-input)))
  (POST "/user/create"
        {:params {:name (ef/from "#reg-user-name" (ef/read-form-input))
                  :login (ef/from "#reg-user-login" (ef/read-form-input))
                  :password (ef/from "#reg-user-password" (ef/read-form-input))}
         :handler user-saved
         :error-handler client.core.error-handler}))
