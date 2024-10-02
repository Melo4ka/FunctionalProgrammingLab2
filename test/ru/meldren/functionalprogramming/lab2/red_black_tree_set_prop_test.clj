(ns ru.meldren.functionalprogramming.lab2.red-black-tree-set-prop-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.core.match :refer [match]]
            [ru.meldren.functionalprogramming.lab2.red-black-tree-set :refer :all]))

(def gen-set
  (gen/fmap #(apply rb-set %)
            (gen/vector gen/small-integer)))

(defspec test-identity
  100
  (prop/for-all [set gen-set]
    (and (= set (combine set (rb-set)))
         (= set (combine (rb-set) set)))))

(defspec test-associativity
  100
  (prop/for-all [set1 gen-set set2 gen-set set3 gen-set]
    (= (combine set1 (combine set2 set3))
       (combine (combine set1 set2) set3))))

(defspec test-idempotence
  100
  (prop/for-all [set gen-set]
    (= (combine set set) set)))

(defn- valid-all-node-colors [tree]
  (match tree
         nil true
         [:black a _ b] (and (valid-all-node-colors a)
                             (valid-all-node-colors b))
         [:red a _ b] (and (valid-all-node-colors a)
                           (valid-all-node-colors b))
         :else false))

(defspec test-all-node-colors
  100
  (prop/for-all [set gen-set]
    (let [tree (.tree set)]
      (valid-all-node-colors tree))))

(defspec test-root-color
  100
  (prop/for-all [set gen-set]
    (let [tree (.tree set)]
      (match tree
             nil true
             [:black _ _ _] true
             :else false))))

(defn- valid-red-node-colors [tree]
  (match tree
         nil true
         [:black a _ b] (and (valid-red-node-colors a)
                             (valid-red-node-colors b))
         [:red a _ b] (and (match a
                                  nil true
                                  [:black _ _ _] true
                                  :else false)
                           (match b
                                  nil true
                                  [:black _ _ _] true
                                  :else false))
         :else false))

(defspec test-red-node-colors
  100
  (prop/for-all [set gen-set]
    (let [tree (.tree set)]
      (valid-red-node-colors tree))))

(defn- count-black-nodes [tree]
  (match tree
         nil 0
         [:black a _ b] (+ 1 (min (count-black-nodes a) (count-black-nodes b)))
         [:red a _ b] (min (count-black-nodes a) (count-black-nodes b))
         :else 0))

(defn- valid-black-node-paths [tree black-count]
  (match tree
         nil (= black-count 0)
         [:black a _ b] (and (valid-black-node-paths a (dec black-count))
                             (valid-black-node-paths b (dec black-count)))
         [:red a _ b] (and (valid-black-node-paths a black-count)
                           (valid-black-node-paths b black-count))
         :else false))

(defspec test-black-node-count
  100
  (prop/for-all [set gen-set]
    (let [tree (.tree set)
          black-count (count-black-nodes tree)]
      (valid-black-node-paths tree black-count))))
