(ns todomvc-db-view.system
  (:require [org.httpkit.server :as server]
            [ring.middleware.file :as middleware-file]
            [todomvc-db-view.datomic.connection :as datomic]
            [todomvc-db-view.db-view.get :as db-view-get]
            [datomic.api :as d]
            [ring.util.response :as response]))

(defonce system
  (atom nil))

(defn dispatch
  "Dispatches the Ring request to the Ring handler of the system."
  [request]
  (let [system-value @system
        db (d/db (:datomic/con system-value))]
    (or
     (db-view-get/ring-handler db
                               request)
     ;; NOTE: add new Ring handlers here.
     )))

(def app
  (-> dispatch
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
