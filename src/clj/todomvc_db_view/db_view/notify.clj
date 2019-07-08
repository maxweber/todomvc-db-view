(ns todomvc-db-view.db-view.notify
  (:require [org.httpkit.server :as httpkit]
            [datomic.api :as d]
            [todomvc-db-view.util.edn :as edn]))

;; Concept:
;;
;; Provides an API endpoint that allows clients to listen for changes
;; in the Datomic transaction log, which affect their current
;; logged-in user.
;;
;; Thereby the client can refresh the `:db-view/value` as soon as it
;; is notified by this API endpoint. HTTP long polling is used here to
;; allow the server to push a message to the client. It is less
;; complex to implement in comparison to WebSockets and the low
;; latency / overhead of WebSockets is not required for this use case.

(defonce client-listeners-state
  (atom {}))

(defn ring-handler [request]
  (when (and (= (:request-method request) :post)
             (= (:uri request) "/db-view/notify"))
    ;; NOTE: for a production app add an authentication check here:
    (httpkit/with-channel request channel
      ;; register the user's browser session in the
      ;; `client-listeners-state`:
      (swap! client-listeners-state
             assoc
             channel
             channel)
      (httpkit/on-close channel
                        (fn [status]
                          ;; remove the user's browser session
                          ;; from the `client-listeners-state` as
                          ;; soon as the channel is closed (user
                          ;; has closed the browser tab or the
                          ;; network connection was interrupted:
                          (swap! client-listeners-state
                                 dissoc
                                 channel))))))

(defn notify
  "A Datomic transaction listener that notifies all user browser
   sessions, where the user were affected by the transaction of the
   `tx-report`."
  [tx-report]
  (let [basis-t (d/basis-t (:db-after tx-report))
        response (edn/response
                  {:db/basis-t basis-t})]
    ;; NOTE: for a production app only send notifications to the users
    ;;       which are affected by this `tx-report`.
    (doseq [channel (vals @client-listeners-state)]
      (httpkit/send! channel
                     response))))
