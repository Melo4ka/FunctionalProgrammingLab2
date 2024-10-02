(ns ru.meldren.functionalprogramming.lab2.red-black-tree-set-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [ru.meldren.functionalprogramming.lab2.red-black-tree-set :refer :all]))

(deftest test-insert
  (is (= (insert (rb-set) 1) (rb-set 1)))
  (is (= (insert (rb-set 2 3) 1) (rb-set 1 2 3)))
  (is (= (insert (rb-set) "a") (rb-set "a")))
  (is (= (insert (rb-set "b" "c") "a") (rb-set "a" "b" "c"))))

(deftest test-delete
  (let [set (rb-set 1 2 3)]
    (is (= (delete set 1) (rb-set 2 3)))
    (is (= (delete set 4) (rb-set 1 2 3))))
  (let [set (rb-set "a" "b" "c" "d")]
    (is (= (delete set "a") (rb-set "b" "c" "d")))
    (is (= (delete set "d") (rb-set "a" "b" "c")))))

(deftest test-filter
  (is (= (filter-values (rb-set 1 2 3 4 5) even?) (rb-set 2 4)))
  (is (= (filter-values (rb-set "a" "bb" "ccc" "dddd") #(> (count %) 2)) (rb-set "ccc" "dddd"))))

(deftest test-map
  (is (= (map-values (rb-set 1 2 3) inc) (rb-set 2 3 4)))
  (is (= (map-values (rb-set "a" "b" "c") str/upper-case) (rb-set "A" "B" "C"))))

(deftest test-fold-left
  (is (= (fold-left-values (rb-set 1 2 3) 0 -) -6))
  (is (= (fold-left-values (rb-set "a" "b" "c") "" str) "abc")))

(deftest test-fold-right
  (is (= (fold-right-values (rb-set 1 2 3) 0 -) 2))
  (is (= (fold-right-values (rb-set "a" "b" "c") "" str) "cba")))

(deftest test-combine
  (is (= (combine (rb-set 1 2 3) (rb-set 4 5 6)) (rb-set 1 2 3 4 5 6)))
  (is (= (combine (rb-set "a" "b") (rb-set "c" "d")) (rb-set "a" "b" "c" "d"))))
