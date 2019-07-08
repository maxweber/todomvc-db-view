(ns todomvc-db-view.command.send
  (:require [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Concept:
;;
;; Helper namespace to send a command map (from the `:db-view/value`)
;; to the server.

(defn send!
  "Sends the `command` map to the server, returns the response body or
   `false` if the request failed."
  [command]
  (go
    ;; TODO: add retries:
    (let [response (<! (http/request
                         {:request-method :post
                          :url "/command"
                          :edn-params {:command/encrypted command}}))]
      (if (= (:status response)
             200)
        (:body response)
        (do
          ;; TODO: consider how to inform the user about errors.
          false)))))
