(ns todomvc-db-view.datomic.tx-report-queue
  (:require [datomic.api :as d]
            [todomvc-db-view.db-view.notify :as notify]
            [todomvc-db-view.datomic.connection :as con]
            [redelay.core :as rd]))

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

(defonce queue
  (rd/state :start
            (let [tx-report-queue (d/tx-report-queue @con/con)
                  stop (tx-report-listener-loop tx-report-queue)]
              stop)

            :stop
            (this)
            ))
