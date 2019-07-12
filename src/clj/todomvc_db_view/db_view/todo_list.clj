(ns todomvc-db-view.db-view.todo-list
  (:require [datomic.api :as d]
            [todomvc-db-view.command.crypto :as command]))

;; Concept:
;;
;; The db-view part for the todo list UI.

(defn pull
  "Pulls the attributes from the `todo-item-eids` which are required
   for todo list UI."
  [db todo-item-eids]
  (d/pull-many db
               [:db/id :todo/title :todo/done]
               todo-item-eids))

(defn q-all
  "Queries all todo item entity ids."
  [db]
  (d/q
   '[:find
     [?e ...]
     :where
     [?e :todo/title]]
   db))

(defn q-active
  "Queries active todo item entity ids."
  [db]
  (d/q
   '[:find
     [?e ...]
     :where
     [?e :todo/title]
     (or (not [?e :todo/done])
         [?e :todo/done false])]
   db))

(defn q-completed
  "Queries completed todo item entity ids."
  [db]
  (d/q
   '[:find
     [?e ...]
     :where
     [?e :todo/title]
     [?e :todo/done true]]
   db))

;; NOTE: you could use [Datomic
;;       Rules](https://docs.datomic.com/on-prem/query.html#rules)
;;       above to avoid some repetition.

(defn prepare-todo-items
  "Prepares the todo items identified by the `eids` for the db-view
   value."
  [db eids]
  (map
   (fn [todo-list-item]
     (merge
      todo-list-item
      (if-not (:todo/done todo-list-item)
        {:todo/done! (command/encrypt-command
                      {:command/type :todo/done!
                       :db/id (:db/id todo-list-item)})}
        {:todo/active! (command/encrypt-command
                         {:command/type :todo/active!
                          :db/id (:db/id todo-list-item)})})
      {:todo/delete! (command/encrypt-command
                       {:command/type :todo/delete!
                        :db/id (:db/id todo-list-item)})}))
   (pull db
         eids)))

(defn get-view
  "Returns the db-view for the todo list UI."
  [db db-view-params]
  (when-let [params (:todo/list db-view-params)]
    (let [active-eids (q-active db)
          todo-filter (:todo/filter params
                        :all)
          eids (case todo-filter
                 :active
                 active-eids
                 :completed
                 (q-completed db)
                 :all
                 (q-all db))]
      {:todo/list {:todo/list-items (prepare-todo-items db
                                                        eids)
                   :todo/active-count (count active-eids)}})))

(comment
  (require '[clj-http.client :as http])

  (http/request
   {:request-method :post
    :url "http://localhost:8080/db-view/get"
    :body (pr-str {:todo/list
                   {:todo/filter :active}})
    :as :clojure})

  )
