(ns todomvc-db-view.datomic.schema)

;; Concept:
;;
;; Contains the Datomic schema for this application.

(def schema
  [{:db/ident :todo/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "Title of the TODO entry."}

   {:db/ident :todo/done
    :db/valueType :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc "Marks the TODO entry as done."}])
