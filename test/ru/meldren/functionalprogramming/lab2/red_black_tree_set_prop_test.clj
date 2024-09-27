(ns ru.meldren.functionalprogramming.lab2.red-black-tree-set-prop-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [ru.meldren.functionalprogramming.lab2.red-black-tree-set :refer :all]))

(def gen-set
  (gen/fmap #(apply rb-set %)
            (gen/vector gen/small-integer)))

(defspec test-monoid-identity
  100
  (prop/for-all [set gen-set]
    (and (= set (combine set (rb-set)))
         (= set (combine (rb-set) set)))))

(defspec test-monoid-associativity
  100
  (prop/for-all [set1 gen-set set2 gen-set set3 gen-set]
    (= (combine set1 (combine set2 set3))
       (combine (combine set1 set2) set3))))

(defspec prop-test-idempotence
  100
  (prop/for-all [set gen-set]
    (= (combine set set) set)))
