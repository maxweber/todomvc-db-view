(ns todomvc-db-view.util.aes
  (:require [buddy.core.crypto :as crypto]
            [buddy.core.codecs :as codecs]
            [buddy.core.nonce :as nonce]
            [buddy.core.hash :as hash]
            [buddy.core.codecs.base64 :as base64]))

;; Concept:
;;
;; Provides helper functions to use the AES encryption.

(defn base64-string
  "Decodes `byte-array` as Base64 string."
  [byte-array]
  (codecs/bytes->str
   (base64/encode byte-array)))

(defn generate-key
  "Generates a map with the nonce and secret key encoded as Base64."
  [secret]
  {:buddy/iv (base64-string (nonce/random-bytes 16))
   :buddy/key (base64-string (hash/sha256 secret))})

(def default-algo
  {:algorithm :aes128-cbc-hmac-sha256})

(defn encrypt-string
  "Encrypts the `original-string` and returns the encrypted message as
   Base64 string. `key` should have the format returned by
   `generate-key` function."
  [key original-string]
  (-> (crypto/encrypt
       (codecs/to-bytes original-string)
       (base64/decode (:buddy/key key))
       (base64/decode (:buddy/iv key))
       default-algo)
      (base64-string)))

(defn decrypt-string
  "Decrypts the `encrypted-string` and returns the
   original-string. `key` should have the format returned by
   `generate-key` function."  [key encrypted-string]
  (-> (crypto/decrypt
       (base64/decode (codecs/to-bytes encrypted-string))
       (base64/decode (:buddy/key key))
       (base64/decode (:buddy/iv key))
       default-algo)
      (codecs/bytes->str)))

(comment
  ;; Full encrypt decrypt roundtrip:
  (let [key (generate-key "secret")
        encrypted-string (encrypt-string key "hello world")]
    (decrypt-string key encrypted-string)))
