(ns todomvc-db-view.state.core
  (:require [reagent.core :as r]))

;; Concept:
;;
;; Namespace with the Reagent atom `state` that holds the global app
;; state of this ClojureScript app.

(def initial-state-value
  {:db-view/params {:todo/list {}}})

(defonce state
  (r/atom initial-state-value))
