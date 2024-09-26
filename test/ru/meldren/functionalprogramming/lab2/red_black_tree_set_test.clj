(ns ru.meldren.functionalprogramming.lab2.red-black-tree-set-test
  (:require [clojure.test :refer :all]
            [ru.meldren.functionalprogramming.lab2.red-black-tree-set :refer :all]))

(deftest test-insert
  (is (= (insert (rb-set) 1) (rb-set 1)))
  (is (= (insert (rb-set 2 3) 1) (rb-set 1 2 3))))

(deftest test-delete
  (let [set (rb-set 1 2 3)]
    (is (= (delete set 1) (rb-set 2 3)))
    (is (= (delete set 4) (rb-set 1 2 3)))))

(deftest test-filter
  (is (= (filter-values (rb-set 1 2 3 4 5) even?) (rb-set 2 4))))

(deftest test-map
  (is (= (map-values (rb-set 1 2 3) inc) (rb-set 2 3 4))))

(deftest test-fold-left
  (is (= (fold-left-values (rb-set 1 2 3) 0 -) -6)))

(deftest test-fold-right
  (is (= (fold-right-values (rb-set 1 2 3) 0 -) 2)))

(deftest test-combine
  (is (= (combine (rb-set 1 2 3) (rb-set 4 5 6)) (rb-set 1 2 3 4 5 6))))
