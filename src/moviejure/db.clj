(ns moviejure.db
  (:use korma.db
        korma.core
        carica.core))

(defdb db {:classname "org.postgresql.Driver"
           :subprotocol "postgresql"
           :user (config :db :user)
           :password (config :db :pass)
           :subname (str "//" (config :db :host) "/" (config :db :name) "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory")})

(defn get-current-date []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))
