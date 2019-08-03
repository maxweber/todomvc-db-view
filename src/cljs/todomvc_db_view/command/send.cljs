(ns todomvc-db-view.command.send
  (:require [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Concept:
;;
;; Helper namespace to send an encrypted command map to the server.

(defn send!
  "Sends the `encrypted-command` map to the server, returns a channel
   with the response body or `false` if the request failed."
  [encrypted-command]
  (go
    ;; TODO: add retries:
    (let [response (<! (http/request
                        {:request-method :post
                         :url "/command"
                         :body encrypted-command}))]
      (if (= (:status response)
             200)
        (:body response)
        (do
          ;; TODO: consider how to inform the user about errors.
          false)))))
