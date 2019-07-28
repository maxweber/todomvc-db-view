(ns todomvc-db-view.command.todo-item)

(defn done-tx
  "Transaction to mark the todo item as done."
  [command]
  [{:db/id (:db/id command)
    :todo/done true}])

(defn active-tx
  "Transaction to mark the todo item as active."
  [command]
  [{:db/id (:db/id command)
    :todo/done false}])

(defn delete-tx
  "Transaction to delete the todo item"
  [command]
  [[:db/retractEntity (:db/id command)]])

(defn edit-tx
  "Transaction to edit the title of the todo item"
  [command]
  [{:db/id (:db/id command)
    :todo/title (:todo/title command)}])

(defn new-tx
  "Transaction to create a new todo item with a title"
  [command]
  [{:db/id "new TODO"
    :todo/title (:todo/title command)
    :todo/done false}])

(def command-effects
  {:todo/done! done-tx
   :todo/active! active-tx
   :todo/delete! delete-tx
   :todo/edit! edit-tx
   :todo/new! new-tx})
