(ns dev
  (:require [datomic.api :as d]
            [todomvc-db-view.system :as system]))

;; Concept:
;;
;; Namespace that provides tools which are helpful during the
;; development.

(defn db
  "Returns a db value of the current Datomic database."
  []
  (d/db (:datomic/con @system/system)))
