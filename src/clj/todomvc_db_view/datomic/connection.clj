(ns todomvc-db-view.datomic.connection
  (:require [datomic.api :as d]
            [todomvc-db-view.datomic.schema :as schema]))

;; Concept:
;;
;; Establishes a connection to the Datomic database. For this example
;; an in-memory version of Datomic is used.

(def db-uri
  "datomic:mem://todomvc-db-view")

(defn start
  []
  (d/create-database db-uri)
  (let [con (d/connect db-uri)]
    @(d/transact con
                 schema/schema)
    con))

(defn stop
  [system-value]
  (d/release (:datomic/con system-value)))
