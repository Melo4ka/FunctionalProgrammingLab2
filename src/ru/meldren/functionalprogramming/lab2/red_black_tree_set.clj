(ns ru.meldren.functionalprogramming.lab2.red-black-tree-set
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
  (let [insert (fn insert [tree]
                 (match tree
                        nil [:red nil x nil]
                        [color a y b]
                        (let [cmp (compare x y)]
                          (cond
                            (< cmp 0) (balance [color (insert a) y b])
                            (> cmp 0) (balance [color a y (insert b)])
                            :else tree))))
        [_ a y b] (insert tree)]
    [:black a y b]))

(defn- find-val [tree x]
  (match tree
         nil nil
         [_ a y b]
         (let [cmp (compare x y)]
           (cond
             (< cmp 0) (find-val a x)
             (> cmp 0) (find-val b x)
             :else x))))

(defn- find-min [tree]
  (match tree
         nil nil
         [:black nil y _] y
         [:red nil y _] y
         [_ a _ _] (find-min a)))

(defn- remove-min [tree]
  (match tree
         nil nil
         [:red nil _ b] b
         [:black nil _ b] b
         [color a y b] (let [new-a (remove-min a)]
                         (balance [color new-a y b]))))

(defn- remove-val-tree [tree x]
  (match tree
         nil nil
         [color a y b]
         (let [cmp (compare x y)]
           (cond
             (< cmp 0) (let [new-a (remove-val-tree a x)]
                         (balance [color new-a y b]))
             (> cmp 0) (let [new-b (remove-val-tree b x)]
                         (balance [color a y new-b]))
             :else (let [new-b (remove-min b)]
                     (if new-b
                       (let [min-val (find-min b)]
                         (balance [color a min-val (remove-min b)]))
                       a))))))

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
      (fold-right-tree b (f x (fold-right-tree a init f)) f))
    init))

(defprotocol IRedBlackTreeSet
  (insert [this x])
  (delete [this x])
  (filter-values [this pred])
  (map-values [this f])
  (fold-left-values [this init f])
  (fold-right-values [this init f])
  (combine [this other]))

(deftype RedBlackTreeSet [tree]
  IRedBlackTreeSet
  (insert [_ x] (RedBlackTreeSet. (insert-val-tree tree x)))
  (delete [_ x] (RedBlackTreeSet. (remove-val-tree tree x)))
  (filter-values [_ pred] (RedBlackTreeSet. (filter-tree tree pred)))
  (map-values [_ f] (RedBlackTreeSet. (map-tree tree f)))
  (fold-left-values [_ init f] (fold-left-tree tree init f))
  (fold-right-values [_ init f] (fold-right-tree tree init f))
  (combine [_ other] (reduce #(insert %1 %2) (RedBlackTreeSet. tree) (seq other)))
  IPersistentSet
  (equiv [_ other] (= (set (seq (RedBlackTreeSet. tree))) (set (seq other))))
  (empty [_] (RedBlackTreeSet. nil))
  (seq [_] (when tree (map (fn [[_ _ val _]] val) (tree-seq sequential? (fn [[_ left _ right]] (remove nil? [left right])) tree))))
  (get [_ x] (find-val tree x))
  (contains [this x] (boolean (get this x))))

(defn rb-set [& args]
  (reduce #(insert %1 %2) (RedBlackTreeSet. nil) args))
