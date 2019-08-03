(ns todomvc-db-view.mount
  (:require [todomvc-db-view.db-view.get :as db-view]
            [todomvc-db-view.core :as core]
            [todomvc-db-view.db-view.notify :as notify]
            [reagent.core :as r])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Concept:
;;
;; Initializes and mounts the ClojureScript app.

(defn start []
  (r/render-component [core/todo-app]
                      (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads, it is called in the
  ;; index.html and must be exported so it is available even in
  ;; :advanced release builds.
  (go
    (<! (db-view/refresh!))
    (notify/start-listening)
    (start)))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
