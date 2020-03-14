(ns contractskam.specs.user-spec
  (:require [clojure.set :refer [rename-keys]]
            #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [contractskam.specs.common-spec :as cspk]
            [com.rpl.specter :as S]))

;; User

(s/def ::UserID ::cspk/email-type)
(s/def ::Email ::cspk/email-type)
(s/def ::Username ::cspk/non-null-string-type)
(s/def ::Role int?)
(s/def ::Confirmed int?)
(s/def ::PasswordHash ::cspk/non-null-string-type)
(s/def ::LastSeen ::cspk/non-null-string-type)

(s/def ::user-cat (s/keys :req [::UserID]))
(s/def ::user-key (s/keys :req [::UserID ::Email]))
(s/def ::user (s/keys :req [::UserID ::Email]
                      :opt [::Role ::Confirmed ::Username
                            ::PasswordHash ::LastSeen]))

(s/def ::user-like (s/or
                     :c ::user-cat
                     :k ::user-key
                     :u ::user))
(s/def ::many-users-type (s/or
                           :nada empty?
                           :lst (s/* ::user-like)))

;; Helpers

(defn user-keys-localize [m]
  (rename-keys m {:UserID       ::UserID :Email ::Email
                  :Username     ::Username :Role ::Role
                  :Confirmed    ::Confirmed
                  :PasswordHash ::PasswordHash
                  :LastSeen     ::LastSeen}))

(defn user-to-attrvals [m]
  (let [kvec (-> m keys (into []))
        resm (S/multi-transform (S/multi-path [:UserID (S/terminal #(hash-map :S %))]
                                              [:Email (S/terminal #(hash-map :S %))]
                                              [:Username (S/terminal #(hash-map :S %))]
                                              [:Role (S/terminal #(hash-map :N (str %)))]
                                              [:Confirmed (S/terminal #(hash-map :N (str %)))]
                                              [:PasswordHash (S/terminal #(hash-map :S %))]
                                              [:LastSeen (S/terminal #(hash-map :S %))]) m)]
    (select-keys resm kvec)))
