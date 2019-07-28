(ns todomvc-db-view.db-view.todo-edit
  (:require [todomvc-db-view.command.crypto :as command]
            [datomic.api :as d]))

(defn get-view
  "Returns the db-view for the todo list UI."
  [db db-view-input]
  (when-let [params (:todo/edit db-view-input)]
    (when (and (string? (:todo/title params))
               (integer? (:db/id params))
               ;; is a todo entity?
               (:todo/title (d/entity db (:db/id params))))
      (if (> (count (:todo/title params)) 2)
        {:todo/edit {:todo/edit! (command/encrypt-command
                                   (merge
                                     {:command/type :todo/edit!}
                                     (select-keys params [:todo/title :db/id])))}}
        {:todo/edit {:error "Title must be longer than 2 characters!"}}))))
