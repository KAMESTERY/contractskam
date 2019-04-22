(ns contractskam.specs.macros
  (:require [expound.alpha :refer [expound]]
            spell-spec.expound))

;(defmacro okspk? [spk data]
;  `(or (clojure.spec.alpha/valid? ~spk ~data)
;       (clojure.spec.alpha/explain-data ~spk ~data)))

(defmacro okspk? [spk data]
  `(or (clojure.spec.alpha/valid? ~spk ~data)
       (expound ~spk ~data)))

;(defmacro okspk? [spk data]
;  `(if-not (clojure.spec.alpha/valid? ~spk ~data)
;     (throw (clojure.spec.alpha/explain-data ~spk ~data))))
