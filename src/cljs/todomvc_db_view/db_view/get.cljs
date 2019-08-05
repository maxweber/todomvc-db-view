(ns todomvc-db-view.db-view.get
  (:require [cljs.core.async :as a]
            [cljs-http.client :as http]
            [todomvc-db-view.state.core :as state])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Concept:
;;
;; Gets and refreshes the db-view from the server. Provides the server
;; API endpoint '/db-view/get' with the `:db-view/input` map from the
;; app state. Receives the `:db-view/output` map in the API response
;; and stores it in the app state. Reagent will trigger a rerender of
;; all UI components which depend on changed parts of the
;; `:db-view/output`.

(defn get-view
  "Provides the server API endpoint '/db-view/get' with the
   `:db-view/input` map from the app state and returns the API response
   that contains the `:db-view/output` map."
  [state-value]
  (go
    (let [response (<! (http/request
                        {:request-method :post
                         :url "/db-view/get"
                         ;; NOTE: for a production app add
                         ;;       authorization here and prefer the
                         ;;       Transit format:
                         :edn-params (:db-view/input state-value)}))]
      (:body response))))

(defn refresh!
  "Provides the server API endpoint '/db-view/get' with the
   `:db-view/input` map from the app state, receives the
   `:db-view/output` map from the API response and stores it in the app
   state."
  []
  (go
    (:db-view/output
     (swap! state/state
            assoc
            :db-view/output
            (<! (get-view @state/state))))))



