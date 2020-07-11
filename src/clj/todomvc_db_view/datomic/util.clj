(ns todomvc-db-view.datomic.util
  (:require [todomvc-db-view.datomic.connection :as con]
            [datomic.api :as d]))

(defn transact!
  [tx]
  @(d/transact @con/con
               tx)
  )
