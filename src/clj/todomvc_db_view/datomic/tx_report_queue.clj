(ns todomvc-db-view.datomic.tx-report-queue
  (:require [datomic.api :as d]
            [todomvc-db-view.db-view.notify :as notify]))

;; Concept:
;;
;; Listens for transaction reports on the tx-report-queue of
;; Datomic. Calls the notify handler for the db-view to inform the
;; clients that they need to do a refresh.

(defn tx-report-listener-loop
  [tx-report-queue]
  (future
    (loop []
      (let [tx-report (.take tx-report-queue)]
        (when (not= tx-report
                    ::stop)
          (try
            (notify/notify tx-report)
            (catch Exception e
              ;; Note: use proper logging here for a production app:
              (println "warning: notify for tx-report failed"
                       e)))
          (recur)))))
  (fn stop-fn []
    (.put tx-report-queue
          ::stop)))

(defn start!
  [con]
  (let [tx-report-queue (d/tx-report-queue con)]
    {:datomic/tx-report-queue tx-report-queue
     :datomic/tx-report-listener-loop (tx-report-listener-loop tx-report-queue)}))

(defn stop!
  [system-value]
  ((:datomic/tx-report-listener-loop system-value))
  (d/remove-tx-report-queue (:datomic/tx-report-queue system-value)))
