(ns todomvc-db-view.command.crypto
  (:require [todomvc-db-view.util.edn :as edn]
            [todomvc-db-view.util.aes :as aes]
            [datomic.api :as d]))

;; Concept:
;;
;; Provides the tools to encrypt and decrypt command maps. The server
;; uses encryption to prevent that the untrusted client can manipulate
;; the command map or send fake requests to the '/command' API
;; endpoint.
;;
;; The encryption variant is feasible since the db-view is also used
;; for validation. Only the server creates command maps and the client
;; does not need to add additonal entries to the command map. If the
;; user likes to change his last name for example, the client adds the
;; new last name to the `:db-view/input` and refreshes the
;; db-view. The server can validate the new last name and return an
;; encrypted command map with the new last name in the
;; `:db-view/output`. The client can send this command map to the
;; '/command' API endpoint to execute the renaming. In the case of a
;; validation error the server would return it in the `:db-view/output`
;; instead of returning an encrypted command map. Some validation
;; types still needs to be re-checked on the server, for example if
;; you like to ensure unique names. However the '/command' API
;; endpoint can then tell the client via the response body to redo the
;; validation step, so that the user sees the corresponding error
;; message.
;;
;; Also authorization aspects become simpler to handle with this
;; approach. If an user is not allowed to change his last name, the
;; server can just exclude the corresponding command map from the
;; db-view. The client UI only shows the corresponding button or input
;; field, if the corresponding command map is in the
;; `:db-view/output`. The '/command' API endpoint does not re-check if
;; the user is allowed to do this command, since the user would not
;; never receive the corresponding encrypted command via the
;; `:db-view/output`. Expiration dates in the command maps can be used
;; to enforce authorization changes in a given timeout.

(defonce the-key
  (delay
   ;; NOTE: it is important to configure your own stable secrect here,
   ;;       (from an environment variable for example).
   (aes/generate-key (str (java.util.UUID/randomUUID)))
   ))

(defn encrypt-command
  "Encrypts a `command-map` with a new `:command/uuid`."
  [command-map]
  (aes/encrypt-string @the-key
                      (edn/pr-str
                       (assoc command-map
                              ;; Used to ensure that the command is
                              ;; transacted at most once:
                              :command/uuid (d/squuid)))))

(defn decrypt-command
  "Decrypts an `encrypted-command-string`. Throws an exception if the
   encrypted data is invalid / manipulated."
  [encrypted-command-string]
  (try
    (edn/read-string
      (aes/decrypt-string @the-key
                          encrypted-command-string))
    (catch Exception e
      (throw (ex-info "invalid encrypted command"
                      {:encrypted-command-string encrypted-command-string}
                      e)))))

(comment
  ;; Example round trip:

  (decrypt-command (encrypt-command {:command/type :example/command!}))
  )
