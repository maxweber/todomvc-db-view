(ns todomvc-db-view.system
  (:require [org.httpkit.server :as server]
            [ring.middleware.file :as middleware-file]))

(defonce system
  (atom nil))

(def app
  (-> (fn [request])
      (middleware-file/wrap-file "public")))

(defn start
  "Starts the system."
  []
  (reset! system
          {:stop-httpkit (server/run-server #'app
                                            {:port 8080})}))

(defn stop
  "Stops the system."
  []
  (when-let [sys @system]
    ((:stop-httpkit sys))
    (reset! system
            nil)))

(comment
  (start)
  (stop)
  )
