(ns client.login
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn ^:export show_form []
  (ef/at "#login-form" (ef/set-style :display "block")))

(defn ^:export close_form []
  (ef/at "#login-form" (ef/set-style :display "none")))

(defn show-logged-in [data]
  (ef/at "#login"  (ef/content (client.core.logout-refs)))
  (ef/at "#logged-in-as" (ef/content (:name data)))
  (close_form)
  (client.core.set-logged-in true))

(defn show-logged-out [data]
  (ef/at "#login"  (ef/content (client.core.login-refs)))
  (client.core.set-logged-in false))

(defn user-logged-in-changed [data]
  (client.core.load-page()))

(defn ^:export try_login_user []
  (POST "/user/login"
        {:params {:login (ef/from "#user-login" (ef/read-form-input))
                  :password (ef/from "#user-password" (ef/read-form-input))}
         :handler user-logged-in-changed
         :error-handler client.core.error-handler}))

(defn ^:export try_logout_user []
  (POST "/user/logout"
        {:handler user-logged-in-changed
         :error-handler client.core.error-handler}))
