(ns todomvc-db-view.main
  (:require [todomvc-db-view.system :as system]
            dev)
  (:gen-class))

;; Concept:
;;
;; Starts and stops the system (production mode).

(def shutdown-hook
  ;; stops the system on a JVM shutdown:
  (Thread.
   (fn []
     (system/stop!))))

(defn -main [& args]
  (.addShutdownHook (Runtime/getRuntime)
                    shutdown-hook)
  (system/start!))
