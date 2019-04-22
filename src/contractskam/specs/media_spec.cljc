(ns contractskam.specs.media-spec
  (:require [clojure.edn :as edn]
            [clojure.string :refer [capitalize join lower-case split]]
            [clojure.set :refer [rename-keys]]
            [clojure.spec.alpha :as s]
            [camel-snake-kebab.core :as csk]
            [contractskam.specs.common-spec :as cspk]
            [contractskam.specs.thing-spec :as tspk]
            [contractskam.specs.data-spec :as dspk]
            [com.rpl.specter :as S])
  (:import java.util.Base64))

;; Media

(s/def ::ParentDocumentID ::tspk/thing-id-type)  ;; Thing Name for Belonging
(s/def ::Type ::tspk/thing-name-type)            ;; Grouping Thing Name for Browsing
(s/def ::MediaID ::tspk/thing-id-type)
(s/def ::UserID ::cspk/email-type)
(s/def ::Version int?)
(s/def ::Score int?)
(s/def ::Tags ::cspk/tags-type)
(s/def ::CreatedAt ::cspk/non-null-string-type)
(s/def ::UpdatedAt ::cspk/non-null-string-type)
(s/def ::FileUrl ::cspk/non-null-string-type)

(s/def ::media-key (s/keys :req [::ParentDocumentID ::MediaID]))
(s/def ::media-browse-key (s/keys :req [::Type ::MediaID]))
(s/def ::media (s/keys :req [::Type ::UserID ::FileUrl]
                       :opt [::ParentDocumentID ::MediaID
                             ::Tags ::Version ::Score
                             ::CreatedAt ::UpdatedAt]))

(s/def ::media-like (s/or
                     :k ::media-key
                     :kb ::media-browse-key
                     :d ::media))
(s/def ::many-media-type (s/or
                          :nada empty?
                          :lst (s/* ::media-like)))

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

(defn media-keys-localize [m]
  (rename-keys m {:ParentDocumentID ::ParentDocumentID
                  :Type ::Type :MediaID ::MediaID
                  :UserID ::UserID :FileUrl ::FileUrl
                  :Score ::Score :Version ::Version :Tags ::Tags
                  :CreatedAt ::CreatedAt :UpdatedAt ::UpdatedAt}))

(defn media-to-thing [m & {:keys [score version]
                           :or {score 0
                                version 0}}]
  (-> m
      (select-keys [:Type :MediaID :UserID :Tags])
      (rename-keys {:Type :Name
                    :MediaID :ThingID})
      (assoc :Score score
             :Version version
             :CreatedAt (str (now))
             :UpdatedAt (str (now)))))

(defn media-to-association [m & {:keys [score version]
                                 :or {score 0
                                      version 0}}]
  (when (:ParentDocumentID m)
    (-> m
        (select-keys [:ParentDocumentID :MediaID :UserID :Tags])
        (rename-keys {:ParentDocumentID :Name
                      :MediaID :ThingID})
        (assoc :Score score
               :Version version
               :CreatedAt (str (now))
               :UpdatedAt (str (now))))))

(defn media-to-data [m]
  {:pre [(s/valid? ::media (media-keys-localize m))]
   :post [(s/valid? ::dspk/many-data-type (map dspk/data-keys-localize %))]}
  (let [thing-id (:MediaID m)]
    (map (fn [[k v]]
           (hash-map :DataID (str (uuid)) :ThingID thing-id :Key k :Value (if-not (string? v)
                                                                            (pr-str v)
                                                                            v)))
         (-> m
             (dissoc :ParentDocumentID :MediaID :UserID :Tags)
             seq))))

(defn thing-data-to-media [thing data]
  {:pre  [(s/valid? ::tspk/thing (tspk/thing-keys-localize thing))
          (s/valid? ::dspk/many-data-type (map #(dspk/data-keys-localize %) data))]
   :post [(s/valid? ::media (media-keys-localize %))]}
  (-> thing
      (rename-keys {:Name :ParentDocumentID
                    :ThingID :MediaID})
      (merge
       (into {} (map #(hash-map
                       (-> % :Key csk/->PascalCase keyword)
                       (-> % :Value)) data)))))

;; (defn dissociate-media [parent-doc-id & media-ids]
;;   ...) ;; TODO: Implement this!
