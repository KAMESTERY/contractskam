(ns contractskam.specs.data-spec
  (:require [clojure.set :refer [rename-keys]]
            [clojure.spec.alpha :as s]
            [contractskam.specs.common-spec :as cspk]
            [contractskam.specs.thing-spec :as tspk]
            [com.rpl.specter :as S]))

;; Data

(s/def ::DataID ::cspk/non-null-string-type)
(s/def ::ThingID ::tspk/thing-id-type)
(s/def ::Key (s/or
              :k keyword?
              :s string?))
;(s/def ::Value ::cspk/has-some-value-type)
(s/def ::Value ::cspk/non-null-string-type)

(s/def ::data-cat (s/keys :req [::ThingID]))
(s/def ::data-key (s/keys :req [::ThingID ::DataID]))
(s/def ::data (s/keys :req [::ThingID ::DataID ::Key ::Value]))

(s/def ::data-like (s/or
                    :c ::data-cat
                    :k ::data-key
                    :d ::data))
(s/def ::many-data-type (s/or
                         :nada empty?
                         :lst (s/* ::data-like)))

;; Helpers

(defn data-keys-localize [m]
  (rename-keys m {:ThingID ::ThingID :DataID ::DataID
                  :Key     ::Key :Value ::Value}))

(defn data-to-attrvals [m]
  (let [kvec (-> m keys (into []))
        resm (S/multi-transform (S/multi-path [:ThingID (S/terminal #(hash-map :S %))]
                                              [:DataID (S/terminal #(hash-map :S %))]
                                              [:Key (S/terminal #(hash-map :S %))]
                                              [:Value (S/terminal #(hash-map :S %))]) m)]
    (select-keys resm kvec)))
