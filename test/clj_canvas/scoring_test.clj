(ns clj-canvas.scoring-test
  (:require [clojure.test :refer [deftest is testing]]
            [clj-canvas.painting :as painting]
            [clj-canvas.scoring :as scoring]
            [clj-canvas.data :as data]))

(defn build-painting
  [art-card-names]
  (let [art-cards (for [name art-card-names]
                    (get data/art-cards-by-name name))]
    (painting/make-painting art-cards)))

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
  (testing "Repetition"
    (is (= 1 (run-score ["Divine" "Precious" "Truth"] "Repetition")))
    (is (= 0 (run-score ["Wandering" "Fading" "Truth"] "Repetition"))))
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

