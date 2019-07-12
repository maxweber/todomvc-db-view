(ns todomvc-db-view.db-view.todo-new
  (:require [todomvc-db-view.command.crypto :as command]
            [datomic.api :as d]))

(defn get-view
  "Returns the db-view for the todo list UI."
  [db db-view-params]
  (when-let [params (:todo/new db-view-params)]
    {:todo/new {:todo/new! (command/encrypt-command
                             (merge
                               {:command/type :todo/new!}
                               (select-keys params [:todo/title])))}}))