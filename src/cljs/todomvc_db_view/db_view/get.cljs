(ns todomvc-db-view.db-view.get
  (:require [cljs.core.async :as a]
            [cljs-http.client :as http]
            [todomvc-db-view.state.core :as state])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; Concept:
;;
;; Gets and refreshes the db-view from the server. Provides the server
;; API endpoint '/db-view/get' with the `:db-view/params` entry from
;; the app state. Receives the `:db-view/value` in the API response
;; and stores it in the app state. Reagent will trigger a rerender of
;; all UI components that depend on the `:db-view/value` (if the
;; corresponding data has changed). Thereby the UI will contain the
;; latest state from the database to avoid sections with stale data.

(defn get-view
  "Provides the server API endpoint '/db-view/get' with the
   `:db-view/params` entry from the app state and returns the API
   response that contains the `:db-view/value`."
  [state-value]
  (go
    (let [response (<! (http/request
                        {:request-method :post
                         :url "/db-view/get"
                         ;; NOTE: for a production app add authorization here:
                         ;; :headers {"Authorization" ""}

                         ;; NOTE: for a production app prefer the
                         ;;       Transit format:
                         :edn-params (:db-view/params state-value)}))]
      (:body response))))

(defn refresh!
  "Provides the server API endpoint '/db-view/get' with the
   `:db-view/params` entry from the app state, receives the
   `:db-view/value` from the API response and stores it in the app
   state."
  []
  (go
    (:db-view/value
     (swap! state/state
            assoc
            :db-view/value
            (<! (get-view @state/state))))))



