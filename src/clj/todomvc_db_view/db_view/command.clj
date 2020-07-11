(ns todomvc-db-view.db-view.command
  (:require [todomvc-db-view.db-view.get :as get]
            [todomvc-db-view.util.edn :as edn]
            [todomvc-db-view.datomic.connection :as con]))

(defn contains-command?
  [db-view-output command]
  (some (fn [x]
          (= x
             command))
        (tree-seq coll? seq db-view-output)))

(defn ring-handler
  [request]
  (when (and (= (:request-method request) :post)
             (= (:uri request) "/db-view/command"))
    (let [input (edn/read-string (slurp (:body request)))]
      (when-let [command (:command/execute! input)]
        (let [[var-sym & args] command
              previous-output (get/get-view (con/db)
                                            input)]
          (when (contains-command? previous-output
                                   command)
            (apply (find-var var-sym)
                   args)
            (edn/response (get/get-view (con/db)
                                        input))))))))
