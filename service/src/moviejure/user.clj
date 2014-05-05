(ns moviejure.user
  (:import java.security.MessageDigest
           java.math.BigInteger)
  (:use korma.db
        korma.core
        compojure.core
        compojure.handler
        moviejure.db
        [moviejure.response :as resp]))

(defentity users)

(defn- md5 [s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        size (* 2 (.getDigestLength algorithm))
        raw (.digest algorithm (.getBytes s))
        sig (.toString (BigInteger. 1 raw) 16)
        padding (apply str (repeat (- size (count sig)) "0"))]
    (str padding sig)))

(defn- get-current-date []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

(defn- create-user [name login password]
   (if-not (insert users (values {:name name :login login  :password_hash (md5 password) :created (get-current-date)}))
    (resp/edn () 420)
    (resp/edn ())))


(defn- get-valid-user [login password]
  (first (select users (where {:login login :password_hash (md5 password)}))))

(defn- login-user [login password]
  (if-let [user (get-valid-user login password)]
    (assoc (resp/edn {:status :ok :name (:name user)}) :session {:user user})
    (resp/edn {:status :error :message "No such user or wrong password"} 420)
  ))

(defn- logout-user []
  (assoc (resp/edn {:status :ok}) :session {}))

(defn- logged-in-user [user]
  (resp/edn (if user {:status :ok :name (:name user)} false)))

(defroutes user-routes
  (POST "/create" [name login password] (create-user name login password))
  (GET "/loggedin" {{user :user} :session} (logged-in-user user))
  (POST "/login" [login password] (login-user login password))
  (POST "/logout" [] (logout-user)))

