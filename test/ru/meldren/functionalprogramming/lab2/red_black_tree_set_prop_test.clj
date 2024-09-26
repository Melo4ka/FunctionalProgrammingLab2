(ns ru.meldren.functionalprogramming.lab2.red-black-tree-set-prop-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :as check]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [ru.meldren.functionalprogramming.lab2.red-black-tree-set :refer :all]))

(def gen-set
  (gen/fmap rb-set (gen/list gen/small-integer)))

(deftest test-monoid-identity
  (check/quick-check 100
                     (prop/for-all [set gen-set]
                       (and (= set (combine set (rb-set)))
                            (= set (combine (rb-set) set))))))

(deftest test-monoid-associativity
  (check/quick-check 100
                     (prop/for-all [set1 gen-set set2 gen-set set3 gen-set]
                       (= (combine set1 (combine set2 set3))
                          (combine (combine set1 set2) set3)))))

(deftest prop-test-idempotence
  (check/quick-check 100
                     (prop/for-all [set gen-set]
                       (= (combine set set) set))))
