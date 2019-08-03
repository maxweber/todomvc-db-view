(ns todomvc-db-view.command.handler
  (:require [todomvc-db-view.command.crypto :as crypto]
            [todomvc-db-view.util.edn :as edn]
            [todomvc-db-view.command.todo-item :as todo-item]
            [todomvc-db-view.command.todo-items :as todo-items]
            [datomic.api :as d]))

;; Concept:
;;
;; Provides the '/command' API endpoint that receives encrypted
;; command maps from the client. The server encrypts the command map
;; to prevent that someone can manipulate it (assuming the encryption
;; is safe). Any manipulation will lead to an exception while
;; decrypting the `encrypted-command-string`.
;;
;; The `:command/uuid` Datomic attribute is used to prevent replay
;; attacks. Such an UUID is included in every command map and is added
;; to the Datomic transaction that transacts the command. The unique
;; constraint on the `:command/uuid` Datomic attribute ensures that a
;; command is transacted at most once.
;;
;; The '/command' API endpoint should not provide any command
;; batching, it only receives one command at a time. Batching of
;; multiple commands can be implemented by providing a single command
;; that includes multiple command maps. Thereby the server has the
;; control how many commands are processed at once (in the correct
;; order; probably in single database transaction).

(defn handle-command
  "Provides a Ring-handler that receives an encrypted command map and
   executes it. Expects a POST request with an EDN encoded `:body`,
   which includes a map with an entry `:command/encrypted` that has the
   `encrypted-command-string` as value.

   Throws exceptions if:

   - it receives an invalid EDN encoded `:body`

   - the decryption of the `encrypted-command-string` fails, meaning
     it probably was manipulated."
  [command-handler! request]
  (when (and (= (:request-method request) :post)
             (= (:uri request) "/command"))
    ;; NOTE: an authorization check is not necessary here, since
    ;;       through the encryption the authenticity is already
    ;;       ensured. Adding a check-authentication here, would also
    ;;       mean that this endpoint could not handle use cases
    ;;       without authentication like a user registration form.
    (let [params (edn/read-string (slurp (:body request)))
          encrypted-command-string (:command/encrypted params)
          command (crypto/decrypt-command encrypted-command-string)]
      (command-handler! command))))

(defn example-command-handler
  "Just an example command-handler that writes something to stdout."
  [command]
  (println "example-command-handler"
           (edn/pr-str command)))

(defn add-command-uuid
  "Adds the `:command/uuid` to the `datomic-tx` to ensure that the
   command is transacted at most once (avoids replay attacks)."
  [datomic-tx command]
  (cons
   [:db/add "datomic.tx" :command/uuid (:command/uuid command)]
   datomic-tx))

(defn command-handler!
  "A command-effect may perform side-effects and may return a Datomic
   transaction that will be transacted by this command-handler."
  [datomic-connection get-command-effect command]
  (when-let [command-effect (get-command-effect (:command/type command))]
    (when-let [datomic-tx (command-effect
                           (assoc command
                                  ;; provides the current db-value to
                                  ;; the command effect function:
                                  :datomic/db (d/db datomic-connection)))]
      @(d/transact datomic-connection
                   (add-command-uuid datomic-tx
                                     command)))
    (edn/response {:status :ok})))

(defn ring-handler
  [system-value request]
  (handle-command
   (partial command-handler!
            (:datomic/con system-value)
            (merge todo-item/command-effects
                   todo-items/command-effects
                   {:example/command! example-command-handler}
                   ;; NOTE: register new command effects
                   ;;       here.
                   ))
   request))

(comment
  ;; Example of creating and invoking a command:
  (require '[clj-http.client :as http])

  (http/request
   {:request-method :post
    :url "http://localhost:8080/command"
    :body (edn/pr-str
           {:command/encrypted
            (crypto/encrypt-command {:command/type :example/command!})})})

  )
