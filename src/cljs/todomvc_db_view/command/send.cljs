(ns todomvc-db-view.command.send
  (:require [cljs-http.client :as http]
            [todomvc-db-view.state.core :as state])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Concept:
;;
;; Helper namespace to send an encrypted command map to the server.

(defn send!
  "Sends the `encrypted-command` map to the server, returns a channel
   with the response body or `false` if the request failed."
  [command]
  (go
    (swap! state/state
           assoc
           :db-view/command-executing?
           true)
    ;; TODO: add retries:
    (let [input (:db-view/input @state/state)
          body (assoc input
                      :command/execute! command)
          response (<! (http/request
                        {:request-method :post
                         :url "/db-view/command"
                         :body body}))]
      (if (= (:status response)
             200)
        (swap! state/state
               assoc
               :db-view/output
               (:body response)
               :db-view/command-executing?
               false)
        (do
          ;; TODO: consider how to inform the user about errors.
          false)))))
