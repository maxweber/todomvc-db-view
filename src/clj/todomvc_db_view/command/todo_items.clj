(ns todomvc-db-view.command.todo-items
  (:require [datomic.api :as d]
            [todomvc-db-view.datomic.connection :as con]
            [todomvc-db-view.datomic.util :as datomic-util]))

;; Concept:
;;
;; Contains the command transactions for a collection of todo items.

(defn q-todo-item-eids
  "Returns the entity ids of all todo items."
  [db]
  (d/q
   '[:find
     [?e ...]
     :where
     [?e :todo/title]]
   db))

(comment
  (q-todo-item-eids (dev/db))
  )

(defn set-done-tx
  "Transaction to set the `:todo/done` attribute of all entities to
   `done-value`."
  [db done-value]
  (map
   (fn [eid]
     [:db/add eid :todo/done done-value])
   (q-todo-item-eids db)))

(defn q-completed-todo-item-eids
  "Returns the entity ids of completed todo items."
  [db]
  (d/q
   '[:find
     [?e ...]
     :where
     [?e :todo/done true]]
   db))

(defn clear-completed-tx
  "Transaction to remove all todo items which are marked as completed."
  [db]
  (map
   (fn [eid]
     [:db/retractEntity eid])
   (q-completed-todo-item-eids db)))

(defn activate-all!
  []
  (datomic-util/transact! (set-done-tx (con/db)
                                       false)))

(defn complete-all!
  []
  (datomic-util/transact! (set-done-tx (con/db)
                                       true)))

(defn clear-completed!
  []
  (datomic-util/transact! (clear-completed-tx (con/db))))
