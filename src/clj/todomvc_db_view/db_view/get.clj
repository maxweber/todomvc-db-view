(ns todomvc-db-view.db-view.get
  (:require [datomic.api :as d]
            [todomvc-db-view.util.edn :as edn]
            [todomvc-db-view.db-view.todo-list :as todo-list]))

;; Concept:
;;
;; Provides the API endpoint to get the db view. The request body
;; contains the parameters to query the database for the db-view-value
;; that is required for the currently active UI parts. This value is
;; returned in the response body and the client stores it in the
;; Reagent app state atom, where the UI components can access it.

(defn get-view
  "Main entry point to gather the db-view-value for the client. Based on
   the given Datomic database value `db` and the `db-view-params` from
   the client."
  [db db-view-params]
  (merge
   (todo-list/get-view db
                       db-view-params)
   ;; NOTE: add other db-view parts here.
   ))

(defn ring-handler
  "Ring handler to get the `:db-view/value` for the given
   `:db-view/params` in the `request` body."
  [db request]
  (when (and (= (:request-method request) :post)
             (= (:uri request) "/db-view/get"))
    ;; NOTE: for a production app rather use
    ;;       [Transit](https://github.com/cognitect/transit-format)
    ;;       here instead of EDN:
    (let [db-view-params (edn/read-string (slurp (:body request)))]
      ;; NOTE: for a production app do the appropriate authorization
      ;;       checks:
      (edn/response
       (get-view db
                 db-view-params)))))
