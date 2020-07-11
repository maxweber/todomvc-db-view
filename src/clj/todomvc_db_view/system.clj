(ns todomvc-db-view.system
  (:require [org.httpkit.server :as server]
            [ring.middleware.file :as middleware-file]
            [todomvc-db-view.datomic.connection :as datomic]
            [todomvc-db-view.db-view.get :as db-view-get]
            [todomvc-db-view.db-view.notify :as notify]
            [todomvc-db-view.datomic.tx-report-queue :as tx-report-queue]
            [todomvc-db-view.db-view.command :as command]
            [datomic.api :as d]
            [ring.util.response :as response]
            [redelay.core :as rd]))

;; Concept:
;;
;; Holds the `system` map that contains all system components. A
;; system component may have a lifecyle, so that it can be started and
;; stopped. One system component for example is the HTTP server that
;; frees the port, when it is stopped.

(defn dispatch
  "Dispatches the Ring request to the Ring handler of the system."
  [request]
  (or
   (db-view-get/ring-handler request)
   (command/ring-handler request)
   (notify/ring-handler request)
   ;; NOTE: add new Ring handlers here.
   ))

(def app
  ;; The main Ring-handler:
  (-> dispatch
      (middleware-file/wrap-file "public")))

(def server
  (rd/state :start
            (server/run-server #'app
                               {:port 8080})

            :stop
            (this)
            ))

(defn start!
  "Starts the system."
  []
  @tx-report-queue/queue
  @server
 )

(defn stop!
  "Stops the system."
  []
  (rd/stop))

(defn restart!
  "Restarts the system."
  []
  (stop!)
  (start!))

(comment
  (start!)
  (rd/status)
  (restart!)
  )
