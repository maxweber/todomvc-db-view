(ns todomvc-db-view.command.todo-item
  (:require [datomic.api :as d]))

(defn done!
  "Command marks the todo item as done."
  [con command]
  @(d/transact con
               [{:db/id (:db/id command)
                 :todo/done true}]))

(defn active!
  "Command marks the todo item as active."
  [con command]
  @(d/transact con
               [{:db/id (:db/id command)
                 :todo/done false}]))

(defn delete!
  "Command to delete the todo item"
  [con command]
  @(d/transact con
               [[:db/retractEntity (:db/id command)]]))

(defn edit!
  "Command to edit the title of the todo item"
  [con command]
  @(d/transact con
               [{:db/id (:db/id command)
                 :todo/title (:todo/title command)}]))
