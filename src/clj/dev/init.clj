(ns dev.init
  (:require [todomvc-db-view.system :as system]))

;; Concept:
;;
;; This namespace is only loaded for the development mode (see
;; `:init-ns` in the `:dev` leiningen profile). It starts the system:

(defonce start-system
  (system/start!))

