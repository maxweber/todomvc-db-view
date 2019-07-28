(ns todomvc-db-view.db-view.notify
  (:require [cljs-http.client :as http]
            [todomvc-db-view.db-view.get :as get]
            [cljs.core.async :as a])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

;; Concept:
;;
;; Listens for changes in the Datomic transaction log, which affect
;; the current logged-in user. Uses HTTP long polling to allow the API
;; endpoint on the server to push a message. The server will notify
;; the client everytime, when a transaction contains something
;; relevant for the current logged-in user.
;;
;; Due to this mechanism the client can reflect changes that where
;; issued by the server (a finished background job for example) or a
;; change by another user.

(defn start-listening
  "Starts a go-loop that opens a long-polling request to the
   '/db-view/notify' API endpoint. Refreshes the `:db-view/output` in
   the app state, when it receives a HTTP 200 response. Sleeps for a
   short moment, when it receives an error response to not DDoS the
   server in the case of a server issue."
  []
  (go-loop []
    (let [response (<! (http/request
                         {:request-method :post
                          :url "/db-view/notify"}))]
      (if (= (:status response)
             200)
        (<! (get/refresh!))
        (<! (a/timeout 2000)))
      (recur))))
