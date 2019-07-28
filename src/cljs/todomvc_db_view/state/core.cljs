(ns todomvc-db-view.state.core
  (:require [reagent.core :as r]))

;; Concept:
;;
;; Namespace with the Reagent atom `state` that holds the global app
;; state of this ClojureScript app.

(def initial-state-value
  {:db-view/input {:todo/list {}}})

(defonce state
  (r/atom initial-state-value))

(defn cursor
  "Creates a Reagent cursor for the app state with the help of
  `reagent.core/cursor`. The `path` can be a single key(word) or a
   vector of keys."
  [path]
  (r/cursor state
            (if (sequential? path)
              path
              [path])))
