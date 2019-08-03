(ns todomvc-db-view.command.crypto
  (:require [todomvc-db-view.util.edn :as edn]
            [todomvc-db-view.util.aes :as aes]
            [datomic.api :as d]))

;; Concept:
;;
;; Provides tools to encrypt and decrypt command maps. The server uses
;; encryption to prevent that someone can manipulate the command map
;; or send fake requests to the '/command' API endpoint.
;;
;; Only the server creates command maps, they are returned as part of
;; the `:db-view/output` response. The client does not need to add
;; additonal entries to a command map. For example, if the user likes
;; to change his last name, the client adds the new last name to the
;; `:db-view/input` and refreshes the db-view. The server can validate
;; the new last name and return an encrypted command map with the new
;; last name in the `:db-view/output`. The client can send this
;; command map to the '/command' API endpoint to execute the
;; change. In the case of a validation error the server would return
;; it in the `:db-view/output` instead of returning the encrypted
;; command map. Some validation types still needs to be re-checked on
;; the server, for example if you like to ensure unique names. However
;; the '/command' API endpoint can then tell the client via the
;; response body to redo the validation step, so that the user sees
;; the corresponding error message.
;;
;; Also authorization aspects become simpler to handle with this
;; approach. If an user is not allowed to change his last name, the
;; server can just exclude the corresponding command map from the
;; db-view. The client UI only shows the corresponding button or input
;; field, if the command map is in the `:db-view/output`. The
;; '/command' API endpoint does not re-check, if the user is allowed
;; to preform this command, since the user would not have received the
;; corresponding encrypted command via the `:db-view/output` in the
;; first place. Expiration dates in the command maps can be used to
;; enforce authorization changes in a given timeout.

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
