(ns client.login
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(defn ^:export show_form []
  (ef/at "#login-form" (ef/set-style :display "block")))

(defn ^:export close_form []
  (ef/at "#login-form" (ef/set-style :display "none")))

(defn user-logged-in [data]
  (ef/at "#login"  (ef/content (client.core.logout-refs)))
  (ef/at "#logged-in-as" (ef/content (:name data)))
  (close_form))

(defn user-logged-out [data]
  (ef/at "#login"  (ef/content (client.core.login-refs))))

(defn ^:export try_login_user []
      (.log js/console (ef/from "#user-login" (ef/read-form-input)))
  (.log js/console (ef/from "#user-password" (ef/read-form-input)))
  (POST "/user/login"
        {:params {:login (ef/from "#user-login" (ef/read-form-input))
                  :password (ef/from "#user-password" (ef/read-form-input))}
         :handler user-logged-in
         :error-handler client.core.error-handler}))

(defn ^:export try_logout_user []
  (POST "/user/logout"
        {:handler user-logged-out
         :error-handler client.core.error-handler}))
