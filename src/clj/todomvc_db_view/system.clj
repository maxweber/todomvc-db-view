(ns todomvc-db-view.system
  (:require [org.httpkit.server :as server]
            [ring.middleware.file :as middleware-file]
            [todomvc-db-view.datomic.connection :as datomic]))

(defonce system
  (atom nil))

(def app
  (-> (fn [request])
      (middleware-file/wrap-file "public")))

(defn start!
  "Starts the system."
  []
  (reset! system
          {:datomic/con (datomic/start)
           :stop-httpkit (server/run-server #'app
                                            {:port 8080})}))

(defn stop!
  "Stops the system."
  []
  (when-let [sys @system]
    ((:stop-httpkit sys))
    (datomic/stop sys)
    (reset! system
            nil)))

(defn restart!
  []
  (stop!)
  (start!))

(comment
  (restart!)
  )
