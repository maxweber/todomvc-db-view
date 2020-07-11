(ns todomvc-db-view.db-view.todo-list
  (:require [datomic.api :as d]
            [todomvc-db-view.command.todo-items :as todo-items]
            [todomvc-db-view.datomic.util :as datomic-util]
            ))

;; Concept:
;;
;; The db-view part for the todo list UI.

(defn q-all
  "Queries all todo-item entities and pulls the attributes which are
   required for todo list UI."
  [db]
  (d/q
   '[:find
     [(pull ?e
            [:db/id :todo/title :todo/done]) ...]
     :where
     [?e :todo/title]]
   db))

(defn commands
  "Prepares the commands for the `todo-item`."
  [todo-item]
  (let [db-id (:db/id todo-item)
        transact! (symbol #'datomic-util/transact!)]
    (merge
     {:todo/delete! [transact!
                     [[:db/retractEntity db-id]]
                     ]}
     (if-not (:todo/done todo-item)
       {:todo/done! [transact! [{:db/id db-id
                                 :todo/done true}]]}
       {:todo/active! [transact! [{:db/id db-id
                                   :todo/done false}]]})
     )))

(defn get-view
  "Returns the db-view for the todo list UI."
  [db db-view-input]
  (when-let [params (:todo/list db-view-input)]
    (let [all (q-all db)
          {:keys [active completed]} (group-by (fn [todo-item]
                                                 (if (:todo/done todo-item)
                                                   :completed
                                                   :active))
                                               all)
          todo-items (case (:todo/filter params)
                       :active
                       active
                       :completed
                       completed
                       all)]
      {:todo/list {:todo/list-items (map
                                     (fn [todo-item]
                                       (merge todo-item
                                              (commands todo-item)))
                                     todo-items)
                   :todo/active-count (count active)
                   :todo/completed-count (count completed)
                   :todo/complete-all! [(symbol #'todo-items/complete-all!)]
                   :todo/activate-all! [(symbol #'todo-items/activate-all!)]
                   :todo/clear-completed! [(symbol #'todo-items/clear-completed!)]}})))

(comment
  (require '[clj-http.client :as http])

  (http/request
   {:request-method :post
    :url "http://localhost:8080/db-view/get"
    :body (pr-str {:todo/list
                   {:todo/filter :active}})
    :as :clojure})

  )
