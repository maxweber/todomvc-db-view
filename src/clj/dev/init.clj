(ns dev.init
  (:require [todomvc-db-view.system :as system]
            [dev]))

(defonce start-system
  (system/start!))

