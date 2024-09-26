(ns ru.meldren.functionalprogramming.lab1.red-black-tree
  (:require [clojure.core.match :refer [match]])
  (:import [clojure.lang IPersistentSet]))

(defn- balance [tree]
  (match [tree]
         [(:or
            [:black [:red [:red a x b] y c] z d]
            [:black [:red a x [:red b y c]] z d]
            [:black a x [:red [:red b y c] z d]]
            [:black a x [:red b y [:red c z d]]])]
         [:red [:black a x b] y [:black c z d]]
         :else tree))

(defn- insert-val-tree [tree x]
  (let [ins (fn ins [tree]
              (match tree
                     nil [:red nil x nil]
                     [color a y b] (cond
                                     (< x y) (balance [color (ins a) y b])
                                     (> x y) (balance [color a y (ins b)])
                                     :else tree)))]
    (let [[_ a y b] (ins tree)]
      [:black a y b])))

(defn- find-val [tree x]
  (match tree
         nil nil
         [_ a y b] (cond
                     (< x y) (find-val a x)
                     (> x y) (find-val b x)
                     :else x)))

(defn- find-min [tree]
  (match tree
         nil nil
         [:black nil y _] y
         [:red nil y _] y
         [color a _ _] (find-min a)))

(defn- remove-min [tree]
  (match tree
         nil nil
         [:red nil y b] b
         [:black nil y b] b
         [color a y b] (let [new-a (remove-min a)]
                         (balance [color new-a y b]))))

(defn- remove-val-tree [tree x]
  (match tree
         nil nil
         [color a y b] (cond
                         (< x y) (let [new-a (remove-val-tree a x)]
                                   (balance [color new-a y b]))
                         (> x y) (let [new-b (remove-val-tree b x)]
                                   (balance [color a y new-b]))
                         :else (let [new-b (remove-min b)]
                                 (if new-b
                                   (let [min-val (find-min b)]
                                     (balance [color a min-val (remove-min b)]))
                                   a)))))

(defn- filter-tree [tree pred]
  (when tree
    (match tree
           [:black a x b]
           (let [left (filter-tree a pred)
                 right (filter-tree b pred)]
             (cond
               (pred x) [:black left x right]
               (some? left) [:black left x right]
               :else right))

           [:red a x b]
           (let [left (filter-tree a pred)
                 right (filter-tree b pred)]
             (cond
               (pred x) [:red left x right]
               (some? left) [:red left x right]
               :else right)))))

(defn- map-tree [tree f]
  (when tree
    (match tree
           [:black a x b]
           [:black (map-tree a f) (f x) (map-tree b f)]

           [:red a x b]
           [:red (map-tree a f) (f x) (map-tree b f)])))

(defn- fold-left-tree [tree init f]
  (if tree
    (let [[_ a x b] tree]
      (fold-left-tree b (f (fold-left-tree a init f) x) f))
    init))

(defn- fold-right-tree [tree init f]
  (if tree
    (let [[_ a x b] tree]
      (fold-right-tree a (f x (fold-right-tree b init f)) f))
    init))

(defprotocol IRedBlackTreeSet
  (insert [this x])
  (delete [this x])
  (filter-values [this pred])
  (map-values [this f])
  (fold-left-values [this init f])
  (fold-right-values [this init f]))

(deftype RedBlackTreeSet [tree]
  IRedBlackTreeSet
  (insert [_ x] (RedBlackTreeSet. (insert-val-tree tree x)))
  (delete [_ x] (RedBlackTreeSet. (remove-val-tree tree x)))
  (filter-values [_ pred] (RedBlackTreeSet. (filter-tree tree pred)))
  (map-values [_ f] (RedBlackTreeSet. (map-tree tree f)))
  (fold-left-values [_ init f] (fold-left-tree tree init f))
  (fold-right-values [_ init f] (fold-right-tree tree init f))
  IPersistentSet
  (empty [_] (RedBlackTreeSet. nil))
  (seq [_] (when tree (map (fn [[_ _ val _]] val) (tree-seq sequential? (fn [[_ left _ right]] (remove nil? [left right])) tree))))
  (get [_ x] (find-val tree x))
  (contains [this x] (boolean (get this x))))

(defn rb-set [& args]
  (reduce #(insert %1 %2) (RedBlackTreeSet. nil) args))

;; TEST SECTION

(def test-set (rb-set 3 5 6 8 9))
(println (empty RedBlackTreeSet))
(println test-set)
(println (insert test-set 7))
(println (delete test-set 6))
(println (contains? test-set 9))
(println (contains? test-set 91))
(println (seq test-set))
(println (map-values test-set even?))
(println (filter-values test-set even?))
(println (fold-left-values test-set 0 -))
(println (fold-right-values test-set 0 -))
