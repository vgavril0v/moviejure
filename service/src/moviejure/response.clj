(ns moviejure.response)

(defn response [data content-type & [status]]
  {:status (or status 200)
   :headers {"Content-Type" content-type}
   :body (pr-str data)})

(defn edn [data & [status]]
  (response data "application/edn" status))

(defn html [data & [status]]
  (response data "text/html" status))
