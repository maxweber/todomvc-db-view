(ns todomvc-db-view.command.todo-items
  (:require [datomic.api :as d]))

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
  [command]
  (set-done-tx (:datomic/db command)
               true))

(defn activate-all-tx
  [command]
  (set-done-tx (:datomic/db command)
               false))

(def command-effects
  {:todo/activate-all! activate-all-tx
   :todo/complete-all! complete-all-tx})
