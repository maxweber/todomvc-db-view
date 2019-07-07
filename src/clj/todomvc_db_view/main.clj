(ns todomvc-db-view.main
  (:require [todomvc-db-view.server :as server])
  (:gen-class))

;; Concept:
;;
;; Starts and stops the system.

(def shutdown-hook
  ;; stops the system on a JVM shutdown:
  (Thread.
   (fn []
     (server/stop!))))

(defn -main [& args]
  (.addShutdownHook (Runtime/getRuntime)
                    shutdown-hook)
  (server/start!))
