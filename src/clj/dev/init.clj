(ns dev.init
  (:require [todomvc-db-view.system :as system]))

(defonce start-system
  (system/start!))

