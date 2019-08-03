(ns todomvc-db-view.command.todo-items
  (:require [datomic.api :as d]))

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

(defn complete-all-tx
  "Transaction to mark all todo items as completed."
  [command]
  (set-done-tx (:datomic/db command)
               true))

(defn activate-all-tx
  "Transaction to mark all todo items as active."
  [command]
  (set-done-tx (:datomic/db command)
               false))

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
  [command]
  (map
   (fn [eid]
     [:db/retractEntity eid])
   (q-completed-todo-item-eids (:datomic/db command))))

(def command-effects
  {:todo/activate-all! activate-all-tx
   :todo/complete-all! complete-all-tx
   :todo/clear-completed! clear-completed-tx})
