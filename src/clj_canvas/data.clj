(ns clj-canvas.data
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [clj-canvas.coll :as coll]
            [clj-canvas.data :as data])
  (:gen-class))

(def ribbons [:red :green :blue :purple])
(def slots [:red :yellow :green :blue :purple])
(def bonus-map {:bonus-tone :tone
                :bonus-hue :hue
                :bonus-texture :texture
                :bonus-shape :shape})
(def bonuses (set (keys bonus-map)))
(def elements (set (vals bonus-map)))
(def icons (set/union elements bonuses))

(def art-cards
  (->> "art-cards.edn"
       io/resource
       slurp
       edn/read-string
       :cards))

(def art-cards-by-name
  (coll/index-by
   (fn [card]
     (or (:adjective card) (:noun card)))
   art-cards))