(ns contractskam.specs.thing-spec
  (:require [clojure.set :refer [rename-keys]]
            #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            #?(:clj  [clojure.spec.gen.alpha :as gen]
               :cljs [cljs.spec.gen.alpha :as gen])
            [contractskam.specs.common-spec :as cspk]
            [com.rpl.specter :as S]))

;; Generators

(def thing-name-gen
  "Generator for thing name"
  (gen/fmap
    (fn [[namespace category]]
      (str namespace ":##:" category))
    (gen/tuple
      cspk/non-empty-string-alphanumeric
      cspk/non-empty-string-alphanumeric)))

(def thing-id-gen
  "Generator for thing id"
  (gen/fmap
    (fn [[namespace category uniq]]
      (str namespace ":##:" category ":##:" uniq))
    (gen/tuple
      cspk/non-empty-string-alphanumeric
      cspk/non-empty-string-alphanumeric
      cspk/non-empty-string-alphanumeric)))

;; Things

(def thing-name-regex #"^[a-zA-Z0-9._-]+:##:+[a-zA-Z0-9._-]{2,400}$")
(s/def ::thing-name-type (s/with-gen
                           (s/and string? #(not= "" %) #(re-matches thing-name-regex %))
                           (fn [] thing-name-gen)))

(def thing-id-regex #"^[a-zA-Z0-9._-]+:##:+[a-zA-Z0-9._-]+:##:+[a-zA-Z0-9.%+-]{2,400}$")
(s/def ::thing-id-type (s/with-gen
                         (s/and string? #(not= "" %) #(re-matches thing-id-regex %))
                         (fn [] thing-id-gen)))

(s/def ::thing-name ::thing-name-type)
(s/def ::thing-id ::thing-id-type)

(s/def ::Name (s/or
                :n ::thing-name-type
                :t ::thing-id-type))
(s/def ::ThingID ::thing-id-type)
(s/def ::UserID ::cspk/email-type)
(s/def ::Tags ::cspk/tags-type)
(s/def ::Score int?)
(s/def ::Version int?)
(s/def ::CreatedAt ::cspk/non-null-string-type)
(s/def ::UpdatedAt ::cspk/non-null-string-type)

(s/def ::thing-cat (s/keys :req [::Name]))
(s/def ::thing-key (s/keys :req [::Name ::ThingID]))
(s/def ::thing (s/keys :req [::Name ::ThingID ::UserID
                             ::CreatedAt ::UpdatedAt]
                       :opt [::Score ::Version ::Tags]))

(s/def ::thing-like (s/or
                      :c ::thing-cat
                      :k ::thing-key
                      :t ::thing))
(s/def ::many-things-type (s/or
                            :nada empty?
                            :lst (s/* ::thing-like)))

;; Helpers

(defn thing-keys-localize [m]
  (rename-keys m {:Name      ::Name :ThingID ::ThingID
                  :UserID    ::UserID :Tags ::Tags
                  :Score     ::Score :Version ::Version
                  :CreatedAt ::CreatedAt
                  :UpdatedAt ::UpdatedAt}))

(defn thing-to-attrvals [m]
  (let [kvec (-> m keys (into []))
        resm (S/multi-transform (S/multi-path [:Name (S/terminal #(hash-map :S %))]
                                              [:ThingID (S/terminal #(hash-map :S %))]
                                              [:UserID (S/terminal #(hash-map :S %))]
                                              [:Tags (S/terminal #(hash-map :SS %))]
                                              [:Score (S/terminal #(hash-map :N (str %)))]
                                              [:Version (S/terminal #(hash-map :N (str %)))]
                                              [:CreatedAt (S/terminal #(hash-map :S %))]
                                              [:UpdatedAt (S/terminal #(hash-map :S %))]) m)]
    (select-keys resm kvec)))
