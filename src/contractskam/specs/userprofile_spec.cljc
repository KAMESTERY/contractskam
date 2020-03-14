(ns contractskam.specs.userprofile-spec
  (:require [clojure.set :refer [rename-keys]]
            #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [contractskam.specs.common-spec :as cspk]
            [com.rpl.specter :as S]))

;; UserProfile

(s/def ::UserID ::cspk/email-type)
(s/def ::Name ::cspk/non-null-string-type)
(s/def ::Age int?)
(s/def ::AvatarHash ::cspk/non-null-string-type)
(s/def ::Location ::cspk/non-null-string-type)
(s/def ::MemberSince ::cspk/non-null-string-type)
(s/def ::AboutMe ::cspk/non-null-string-type)

(s/def ::userprofile-key (s/keys :req [::UserID]))
(s/def ::userprofile (s/keys :req [::UserID ::AvatarHash ::AboutMe]
                             :opt [::Name ::Age ::Location
                                   ::MemberSince]))

(s/def ::userprofile-like (s/or
                            :k ::userprofile-key
                            :up ::userprofile))
(s/def ::many-userprofiles-type (s/or
                                  :nada empty?
                                  :lst (s/* ::userprofile-like)))

;; Helpers

(defn userprofile-keys-localize [m]
  (rename-keys m {:UserID   ::UserID :Name ::Name
                  :Age      ::Age :AvatarHash ::AvatarHash
                  :Location ::Location :MemberSince ::MemberSince
                  :AboutMe  ::AboutMe}))

(defn userprofile-to-attrvals [m]
  (let [kvec (-> m keys (into []))
        resm (S/multi-transform (S/multi-path [:UserID (S/terminal #(hash-map :S %))]
                                              [:Name (S/terminal #(hash-map :S %))]
                                              [:Age (S/terminal #(hash-map :N (str %)))]
                                              [:AvatarHash (S/terminal #(hash-map :S %))]
                                              [:Location (S/terminal #(hash-map :S %))]
                                              [:MemberSince (S/terminal #(hash-map :S %))]
                                              [:AboutMe (S/terminal #(hash-map :S %))]) m)]
    (select-keys resm kvec)))
