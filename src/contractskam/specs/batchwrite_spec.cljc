(ns contractskam.specs.batchwrite-spec
  (:require [clojure.set :refer [rename-keys]]
            [clojure.spec.alpha :as s]
            [com.rpl.specter :as S]))

;; BatchWriteItems

(s/def ::Puts (s/* some?))
(s/def ::Deletes (s/* some?))
(s/def ::domain-batch (s/or
                       :d (s/keys :req [::Deletes])
                       :p (s/keys :req [::Puts])
                       :dp (s/keys :req [::Puts ::Deletes])))
(s/def ::batch-write-items (s/map-of keyword? ::domain-batch))

;; Helpers

(defn batch-keys-localize [m]
  (S/multi-transform
   (S/multi-path [:Things (S/terminal
                           #(rename-keys % {:Puts ::Puts :Deletes ::Deletes}))]
                 [:Data (S/terminal
                         #(rename-keys % {:Puts ::Puts :Deletes ::Deletes}))]) m))
