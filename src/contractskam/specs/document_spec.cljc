(ns contractskam.specs.document-spec
  (:require [clojure.edn :as edn]
            [clojure.string :refer [capitalize join lower-case split]]
            [clojure.set :refer [rename-keys]]
            #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])
            [camel-snake-kebab.core :as csk]
            [contractskam.specs.common-spec :as cspk]
            [contractskam.specs.thing-spec :as tspk]
            [contractskam.specs.data-spec :as dspk]
            [contractskam.specs.media-spec :as mspk]
            [com.rpl.specter :as S])
  (:import java.util.Base64))

;; Document

(s/def ::UserID ::cspk/email-type)
(s/def ::Slug ::cspk/slug-type)
(s/def ::Topic ::tspk/thing-name-type)
(s/def ::DocumentID ::tspk/thing-id-type)
(s/def ::Publish boolean?)
(s/def ::FiltreVisuel int?)
(s/def ::Langue int?)
(s/def ::Niveau int?)
(s/def ::Version int?)
(s/def ::Score int?)
(s/def ::Title ::cspk/non-null-string-type)
(s/def ::Identifier ::cspk/non-null-string-type)
(s/def ::Body ::cspk/non-null-string-type)
(s/def ::Media ::mspk/many-media-type)
;; (s/def ::Bag (s/map-of keyword? ::cspk/has-some-value-type))
(s/def ::Extra (s/map-of keyword? ::cspk/has-some-value-type))
(s/def ::Tags ::cspk/tags-type)
(s/def ::CreatedAt ::cspk/non-null-string-type)
(s/def ::UpdatedAt ::cspk/non-null-string-type)

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


(defn now []
  (java.time.LocalDateTime/now))

(defn uuid []
  (java.util.UUID/randomUUID))

(defn update-vals [m val-keys f]
  (reduce #(update-in % [%2] f) m val-keys))

(defn encode [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn decode [to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))

(defn document-keys-localize [m]
  (rename-keys m {:Topic      ::Topic :DocumentID ::DocumentID
                  :UserID     ::UserID :Publish ::Publish
                  :Slug       ::Slug :Langue ::Langue :Body ::Body
                  :Title      ::Title :Score ::Score :Version ::Version
                  :Niveau     ::Niveau :FiltreVisuel ::FiltreVisuel
                  :Identifier ::Identifier :Tags ::Tags     ;:Media ::Media
                  :CreatedAt  ::CreatedAt :UpdatedAt ::UpdatedAt}))

(defn doc-to-thing [m & {:keys [score version]
                         :or   {score   0
                                version 0}}]
  (-> m
      (select-keys [:Topic :DocumentID :UserID :Tags])
      (rename-keys {:Topic      :Name
                    :DocumentID :ThingID})
      (assoc :Score score
             :Version version
             :CreatedAt (str (now))
             :UpdatedAt (str (now)))))

(defn doc-to-data [m]
  {:pre  [(s/valid? ::document (document-keys-localize m))]
   :post [(s/valid? ::dspk/many-data-type (map dspk/data-keys-localize %))]}
  (let [thing-id (:DocumentID m)]
    (map (fn [[k v]]
           (hash-map :DataID (str (uuid)) :ThingID thing-id :Key k :Value (if-not (string? v)
                                                                            (pr-str v)
                                                                            v)))
         (-> m
             (rename-keys {:DocumentID :ThingID})
             (dissoc :Topic :ThingID :DocumentID :UserID :Tags)
             seq))))

(defn thing-data-to-document [thing data]
  {:pre  [(s/valid? ::tspk/thing (tspk/thing-keys-localize thing))
          (s/valid? ::dspk/many-data-type (map #(dspk/data-keys-localize %) data))]
   :post [(s/valid? ::document (document-keys-localize %))]}
  (-> thing
      (rename-keys {:Name    :Topic
                    :ThingID :DocumentID})
      (merge
        (into {} (map #(hash-map
                         (-> % :Key csk/->PascalCase keyword)
                         (-> % :Value)) data)))
      (update-vals [:FiltreVisuel :Langue :Niveau :Publish] edn/read-string)))
