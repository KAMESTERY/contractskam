(ns contractskam.specs.common-spec
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

;; Generators

(def non-empty-string-alphanumeric
  "Generator for non-empty alphanumeric strings"
  (gen/such-that #(not= "" %)
                 (gen/string-alphanumeric)))

(def email-gen
  "Generator for email addresses"
  (gen/fmap
   (fn [[name host tld]]
     (str name "@" host "." tld))
   (gen/tuple
    non-empty-string-alphanumeric
    non-empty-string-alphanumeric
    non-empty-string-alphanumeric)))

;;;; Specs Definition

(s/def ::resource-attrs (s/* keyword?))

(def slug-regex #"^[a-z0-9]+(?:-[a-z0-9]+)*$")
(s/def ::slug-type (s/and string? #(not= "" %) #(re-matches slug-regex %)))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email-type (s/with-gen
                      (s/and string? #(not= "" %) #(re-matches email-regex %))
                      (fn [] email-gen)))

(s/def ::has-some-value-type (s/and any? #(some? %)))

(s/def ::tags-type (s/* (s/and string? #(not= "" %))))

(s/def ::non-null-string-type (s/and string? #(some? %)))

