(ns todomvc-db-view.db-view.todo-new
  (:require [todomvc-db-view.command.crypto :as command]
            [datomic.api :as d]))

(defn get-view
  "Provides the db-view for the `:todo/new!` command that creates new
   todo items."
  [db db-view-input]
  (when-let [params (:todo/new db-view-input)]
    {:todo/new {:todo/new! (command/encrypt-command
                             (merge
                               {:command/type :todo/new!}
                               (select-keys params [:todo/title])))}}))
