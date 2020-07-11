(ns todomvc-db-view.db-view.todo-edit
  (:require [todomvc-db-view.datomic.util :as datomic-util]
            [datomic.api :as d]))

(defn get-view
  "Provides the db-view to validate the input for a `:todo/edit!`
   command."
  [db db-view-input]
  (when-let [params (:todo/edit db-view-input)]
    (when (and (string? (:todo/title params))
               (integer? (:db/id params))
               ;; is it a todo item entity?
               (:todo/title (d/entity db (:db/id params))))
      ;; Example for a validation which ensures that the `:todo/title`
      ;; is longer than 2 characters after it has been edited in the
      ;; client:
      (if (> (count (:todo/title params)) 2)
        {:todo/edit {:todo/edit! [(symbol #'datomic-util/transact!)
                                  [{:db/id (:db/id params)
                                    :todo/title (:todo/title params)}]]}}
        {:todo/edit {:error "Title must be longer than 2 characters!"}}))))
