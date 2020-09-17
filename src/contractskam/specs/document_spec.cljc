(ns contractskam.specs.document-spec
  (:require [clojure.edn :as edn]
            [clojure.string :refer [capitalize join lower-case split]]
            [clojure.set :refer [rename-keys]]
            #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [camel-snake-kebab.core :as csk]
            [contractskam.specs.common-spec :as cspk]
            [contractskam.specs.content-spec :as ctspk]
            [contractskam.specs.data-spec :as dspk]
            [contractskam.specs.media-spec :as mspk]
            [com.rpl.specter :as S]))

;; Document

(s/def ::UserID ::cspk/email-type)
(s/def ::Slug ::cspk/slug-type)
(s/def ::Topic ::ctspk/Namespace)
(s/def ::DocumentID ::ctspk/content-id-type)
(s/def ::Publish boolean?)
(s/def ::FiltreVisuel int?)
(s/def ::Langue int?)
(s/def ::Niveau int?)
(s/def ::Version ::ctspk/Version)
(s/def ::Score ::ctspk/Score)
(s/def ::Title ::cspk/non-null-string-type)
(s/def ::Identifier ::cspk/non-null-string-type)
(s/def ::Body ::cspk/non-null-string-type)
(s/def ::Media ::mspk/many-media-type)
;; (s/def ::Bag (s/map-of keyword? ::cspk/has-some-value-type))
;; (s/def ::Extra (s/map-of keyword? ::cspk/has-some-value-type))
(s/def ::Tags ::ctspk/Tags)
(s/def ::CreatedAt ::ctspk/CreatedAt)
(s/def ::UpdatedAt ::ctspk/UpdatedAt)

(s/def ::document-key (s/keys :req [::Topic ::DocumentID]))
(s/def ::document (s/keys :req [::Topic ::DocumentID
                                ::UserID ::Publish
                                ::Slug ::FiltreVisuel
                                ::Langue ::Niveau
                                ::Identifier ::Title ::Body]
                          :opt [::Media ::Tags ::Version
                                ::Score ::CreatedAt ::UpdatedAt]))

(s/def ::document-like (s/or
                         :k ::document-key
                         :d ::document))
(s/def ::many-document-type (s/or
                              :nada empty?
                              :lst (s/* ::document-like)))



;; Helpers


(defn document-keys-localize [m]
  (rename-keys m {:Topic      ::Topic :DocumentID ::DocumentID
                  :UserID     ::UserID :Publish ::Publish
                  :Slug       ::Slug :Langue ::Langue :Body ::Body
                  :Title      ::Title :Score ::Score :Version ::Version
                  :Niveau     ::Niveau :FiltreVisuel ::FiltreVisuel
                  :Identifier ::Identifier :Tags ::Tags :Media ::Media
                  :CreatedAt  ::CreatedAt :UpdatedAt ::UpdatedAt}))

