(ns contractskam.specs.content-spec
  (:require [clojure.set :refer [rename-keys]]
            #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            #?(:clj  [clojure.spec.gen.alpha :as gen]
               :cljs [cljs.spec.gen.alpha :as gen])
            [contractskam.specs.common-spec :as cspk]
            [com.rpl.specter :as S]))

;; Generators

(def content-name-gen
  "Generator for thing name"
  (gen/fmap
    (fn [[namespace category]]
      (str namespace ":##:" category))
    (gen/tuple
      cspk/non-empty-string-alphanumeric
      cspk/non-empty-string-alphanumeric)))

(def content-id-gen
  "Generator for thing id"
  (gen/fmap
    (fn [[namespace category uniq]]
      (str namespace ":##:" category ":##:" uniq))
    (gen/tuple
      cspk/non-empty-string-alphanumeric
      cspk/non-empty-string-alphanumeric
      cspk/non-empty-string-alphanumeric)))

;; Contents

(def content-name-regex #"^[a-zA-Z0-9._-]+:##:+[a-zA-Z0-9._-]{2,400}$")
(s/def ::content-name-type (s/with-gen
                           (s/and string? #(not= "" %) #(re-matches content-name-regex %))
                           (fn [] content-name-gen)))

(def content-id-regex #"^[a-zA-Z0-9._-]+:##:+[a-zA-Z0-9._-]+:##:+[a-zA-Z0-9.%+-]{2,400}$")
(s/def ::content-id-type (s/with-gen
                         (s/and string? #(not= "" %) #(re-matches content-id-regex %))
                         (fn [] content-id-gen)))

(s/def ::content-name ::content-name-type)
(s/def ::content-id ::content-id-type)

(s/def ::Namespace (s/or
                :n ::content-name-type
                :t ::content-id-type))
(s/def ::ContentID ::content-id-type)
(s/def ::UserID ::cspk/email-type)
(s/def ::Tags ::cspk/tags-type)
(s/def ::Score int?)
(s/def ::Version int?)
(s/def ::CreatedAt ::cspk/non-null-string-type)
(s/def ::UpdatedAt ::cspk/non-null-string-type)

(s/def ::content-cat (s/keys :req [::Namespace]))
(s/def ::content-key (s/keys :req [::Namespace ::ContentID]))
(s/def ::content (s/keys :req [::Namespace ::ContentID ::UserID
                             ::CreatedAt ::UpdatedAt]
                       :opt [::Score ::Version ::Tags]))

(s/def ::content-like (s/or
                      :c ::content-cat
                      :k ::content-key
                      :t ::content))
(s/def ::many-things-type (s/or
                            :nada empty?
                            :lst (s/* ::content-like)))

;; Helpers

(defn content-keys-localize [m]
  (rename-keys m {:Namespace ::Namespace :ContentID ::ContentID
                  :UserID    ::UserID :Tags ::Tags
                  :Score     ::Score :Version ::Version
                  :CreatedAt ::CreatedAt
                  :UpdatedAt ::UpdatedAt}))

(defn content-to-attrvals [m]
  (let [kvec (-> m keys (into []))
        resm (S/multi-transform (S/multi-path [:Namespace (S/terminal #(hash-map :S %))]
                                              [:ContentID (S/terminal #(hash-map :S %))]
                                              [:UserID (S/terminal #(hash-map :S %))]
                                              [:Tags (S/terminal #(hash-map :SS %))]
                                              [:Score (S/terminal #(hash-map :N (str %)))]
                                              [:Version (S/terminal #(hash-map :N (str %)))]
                                              [:CreatedAt (S/terminal #(hash-map :S %))]
                                              [:UpdatedAt (S/terminal #(hash-map :S %))]) m)]
    (select-keys resm kvec)))
