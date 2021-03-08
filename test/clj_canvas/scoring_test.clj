(ns clj-canvas.scoring-test
  (:require [clojure.test :refer [deftest is testing]]
            [clj-canvas.painting :as painting]
            [clj-canvas.scoring :as scoring]
            [clj-canvas.data :as data]))

(defn build-painting
  [art-card-names]
  (let [art-cards (for [name art-card-names]
                    (get data/art-cards-by-name name))]
    (apply painting/make-painting art-cards)))

(defn run-score
  [art-card-names scoring-card-name]
  (let [painting (build-painting art-card-names)
        scoring-fn (:scoring (get scoring/scoring-cards-by-name
                                  scoring-card-name))]
    (scoring-fn painting)))

(deftest test-scoring-conditions
  (testing "Composition"
    (is (= 1 (run-score ["Wandering" "Fading" "Truth"] "Composition")))
    (is (= 0 (run-score ["Divine" "Precious" "Truth"] "Composition"))))
  (testing "Consistency"
    (is (= 1 (run-score ["Wandering" "Fading" "Truth"] "Consistency")))
    (is (= 0 (run-score ["Divine" "Precious" "Truth"] "Consistency"))))
  (testing "Emphasis"
    (is (= 1 (run-score ["Wandering" "Fading" "Truth"] "Emphasis")))
    (is (= 0 (run-score ["Wandering" "Divine" "Expanse"] "Emphasis"))))
  (testing "Hierarchy"
    (is (= 1 (run-score ["Wandering" "Divine" "Expanse"] "Hierarchy")))
    (is (= 0 (run-score ["Wandering" "Fading" "Truth"] "Hierarchy"))))
  (testing "Movement"
    (is (= 2 (run-score ["Peaceful" "Perspective" "Purpose"] "Movement")))
    (is (= 1 (run-score ["Peaceful" "Beauty" "Game"] "Movement")))
    (is (= 0 (run-score ["Heightened" "Peaceful" "Trap"] "Movement"))))
  (testing "Proportion"
    (is (= 1 (run-score ["Wandering" "Divine" "Expanse"] "Proportion")))
    (is (= 0 (run-score ["Heightened" "Peaceful" "Trap"] "Proportion"))))
  (testing "Repetition"
    (is (= 2 (run-score ["Masked" "Fragile" "Freedom"] "Repetition")))
    (is (= 1 (run-score ["Divine" "Precious" "Truth"] "Repetition")))
    (is (= 0 (run-score ["Wandering" "Fading" "Truth"] "Repetition"))))
  (testing "Style"
    (is (= 1 (run-score ["Wandering" "Divine" "Expanse"] "Style")))
    (is (= 0 (run-score ["Wandering" "Fading" "Truth"] "Style"))))
  (testing "Symmetry"
    (is (= 1 (run-score ["Wandering" "Divine" "Expanse"] "Symmetry")))
    (is (= 0 (run-score ["Wandering" "Fading" "Truth"] "Symmetry"))))
  (testing "Variety"
    (is (= 1 (run-score ["Wandering" "Fading" "Truth"] "Variety")))
    (is (= 0 (run-score ["Divine" "Precious" "Truth"] "Variety")))))

(deftest test-score-bonuses
  (testing "With no bonus"
    (is (= 0 (scoring/score-bonuses
              (build-painting ["Wandering" "Fading" "Truth"])))))
  (testing "With 1 bonus"
    (is (= 1 (scoring/score-bonuses
              (build-painting ["Divine" "Precious" "Truth"]))))))

(def game-scoring-cards
  {:red (get scoring/scoring-cards-by-name "Composition")
   :green  (get scoring/scoring-cards-by-name "Variety")
   :blue (get scoring/scoring-cards-by-name "Movement")
   :purple (get scoring/scoring-cards-by-name "Emphasis")})

(deftest test-score-painting
  (testing "Painting with no bonuses"
    (let [painting (build-painting ["Wandering" "Fading" "Truth"])
          expected {:red 1, :green 1, :blue 1, :purple 1, :bonus 0}]
      (is (= (scoring/score-painting game-scoring-cards painting)
             expected))))
  (testing "Painting with bonuses"
    (let [painting (build-painting ["Heightened" "Peaceful" "Trap"])
          expected {:red 0, :green 0, :blue 0, :purple 0, :bonus 1}]
      (is (= (scoring/score-painting game-scoring-cards painting)
             expected)))))

(deftest test-score-ribbons
  (let [scoring-card (get scoring/scoring-cards-by-name "Repetition")]
    (doseq [[ribbons expected-points] {0 0
                                       1 3
                                       2 7
                                       3 11
                                       4 16
                                       5 16}]
      (testing (str "Scoring ribbons: " ribbons " => points: " expected-points)
        (is (= expected-points
               (scoring/score-ribbons scoring-card ribbons)))))))

(deftest test-score-bonus-ribbons
  (doseq [[ribbons expected-points] {0 0
                                     1 2
                                     2 4
                                     3 6
                                     4 8}]
    (testing (str "Bonus ribbons: " ribbons " => points: " expected-points)
      (is (= expected-points (scoring/score-bonus-ribbons ribbons))))))