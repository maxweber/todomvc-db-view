(ns todomvc-db-view.db-view.get
  (:require [datomic.api :as d]
            [todomvc-db-view.util.edn :as edn]
            [todomvc-db-view.db-view.todo-list :as todo-list]
            [todomvc-db-view.db-view.todo-edit :as todo-edit]
            [todomvc-db-view.db-view.todo-new :as todo-new]))

;; Concept:
;;
;; Provides the API endpoint to get the db view. The request body
;; contains the parameters to query the database for the db-view-value
;; that is required for the currently active UI parts. This value is
;; returned in the response body and the client stores it in the
;; Reagent app state atom, where the UI components can access it.

(defn get-view
  "Main entry point to gather the db-view-value for the client. Based on
   the given Datomic database value `db` and the `db-view-input` from
   the client."
  [db db-view-input]
  (merge
   (todo-list/get-view db
                       db-view-input)
   (todo-edit/get-view db
                       db-view-input)
   (todo-new/get-view db
                      db-view-input)
   ;; NOTE: add other db-view parts here.
   ))

(defn ring-handler
  "Ring handler to get the `:db-view/output` for the given
   `:db-view/input` in the `request` body."
  [db request]
  (when (and (= (:request-method request) :post)
             (= (:uri request) "/db-view/get"))
    ;; NOTE: for a production app rather use
    ;;       [Transit](https://github.com/cognitect/transit-format)
    ;;       here instead of EDN:
    (let [db-view-input (edn/read-string (slurp (:body request)))]
      ;; NOTE: for a production app do the appropriate authorization
      ;;       checks:
      (edn/response
       (get-view db
                 db-view-input)))))
