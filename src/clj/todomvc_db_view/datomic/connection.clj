(ns todomvc-db-view.datomic.connection
  (:require [datomic.api :as d]
            [todomvc-db-view.datomic.schema :as schema]
            [todomvc-db-view.datomic.example-data :as example-data]
            [redelay.core :as rd]))

;; Concept:
;;
;; Establishes a connection to the Datomic database. For this example
;; an in-memory version of Datomic is used.

(def db-uri
  "datomic:mem://todomvc-db-view")

(defonce con
  (rd/state :start
            (d/create-database db-uri)
            (let [con* (d/connect db-uri)]
              @(d/transact con*
                           schema/schema)
              @(d/transact con*
                           example-data/example-data)
              con*)

            :stop
            (d/release this)))

(defn db
  []
  (d/db @con))
