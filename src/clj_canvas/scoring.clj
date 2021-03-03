(ns clj-canvas.scoring
  (:require [clj-canvas.data :as data]
            [clj-canvas.coll :as coll]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.math.numeric-tower :as Math])
  (:gen-class))

(def scoring-fns-by-card-name
  {"Composition" ; Score if all 5 of the swatches have icons
    ; Bonus icons are also counted as filling swatches
   (fn [painting]
     (if (every?
          (fn [swatch-key]
            (swatch-key (:swatches painting)))
          data/swatches)
       1
       0))
   "Consistency" ; Score with exactly 6 visible elements
   (fn [painting]
     (if (= 6
            (count
             (filter #(contains? data/elements %)
                     (flatten (vals (:swatches painting))))))
       1
       0))
   "Emphasis" ; Exactly 1 color hue element
   (fn [painting]
     (if (= 1
            (count
             (filter #(= :hue %)
                     (flatten (vals (:swatches painting))))))
       1
       0))
   "Hierarchy" ; The number of tone elements is greater than or equal to
   ; the number of any other element
   (fn [painting]
     (let [chosen-element :tone
           other-elements (set/difference data/elements #{chosen-element})
           elements (filter #(contains? data/elements %)
                            (flatten (vals (:swatches painting))))
           counts (coll/count-by-values elements)
           chosen-count (chosen-element counts 0)
           other-count (apply max (vals (select-keys counts other-elements)))]
       (if (>= chosen-count other-count)
         1
         0)))
   "Movement" ; 3 matching elements in a row
   (fn [_] 0)
   "Proportion" ; At least 3 of one element and at least 2 of another element
   (fn [_] 0)
   "Proximity" ; Sets of shate and hue elements in adjacent swatches.
   ; Note: each element can only be used in one set
   (fn [_] 0)
   "Repetition" ; Score pairs of 2 shape elements
   (fn [painting]
     (int
      (Math/floor
       (/ (apply max
                 (for [combo (:swatch-combinations painting)]
                   (count (filter #(= :shape %) (vals combo))))) 2))))
   "Space" ; Hue element and a non-adjacent shape element
   ; Can be scored more than once
   ; Each element can only be counted once per scoring condition
   (fn [_] 0)
   "Style" ; At least 3 tone elements
   (fn [painting]
     (if (<= 3
             (count
              (filter #(= :tone %)
                      (flatten (vals (:swatches painting))))))
       1
       0))
   "Symmetry" ; 2 matching elements in either of the swatch pairs:
   ; (red and purple) or (yellow and blue)
   (fn [_] 0)
   "Variety" ; Score if all elements are present at least once
   (fn [painting]
     (if (set/superset?
          (set (flatten (vals (:swatches painting))))
          data/elements)
       1
       0))})

(def scoring-cards
  (for [card (->> "scoring-cards.edn"
                  io/resource
                  slurp
                  edn/read-string
                  :cards)]
    (assoc card
           :scoring
           (get scoring-fns-by-card-name (:name card)))))

; Ensure that all scoring cards have a scoring function
(assert (every? :scoring scoring-cards))

(def scoring-cards-by-name
  (coll/index-by :name scoring-cards))

(defn score-bonuses
  [painting]
  (defn occurance-map
    [allow-list coll]
    (coll/count-by-values (filter (fn [x] (contains? allow-list x)) coll)))
  (let [icons (flatten (vals (:swatches painting)))
        elements (occurance-map data/elements icons)
        bonuses (occurance-map data/bonuses icons)]
    (reduce + (for [[bonus mult] bonuses]
                (* mult (get elements
                             (get data/bonus-map bonus)
                             0))))))
