(ns todomvc-db-view.db-view.todo-new
  (:require [todomvc-db-view.datomic.util :as datomic-util]
            [datomic.api :as d]))

(defn get-view
  "Provides the db-view for the `:todo/new!` command that creates new
   todo items."
  [db db-view-input]
  (when-let [params (:todo/new db-view-input)]
    {:todo/new {:todo/new! [(symbol #'datomic-util/transact!)
                            [{:db/id "new TODO"
                              :todo/title (:todo/title params)
                              :todo/done false}]]}}))

