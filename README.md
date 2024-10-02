# Лабораторная работа №2: Функциональное программирование

**Студент**: Андриенко Сергей Вячеславович

**Группа**: P34092

**Вариант**: Red-black Tree Set

## Цель работы

Освоиться с построением пользовательских типов данных, полиморфизмом, рекурсивными алгоритмами и средствами
тестирования (unit testing, property-based testing).

## Требования

1. Функции:
    * добавление и удаление элементов;
    * фильтрация;
    * отображение (map);
    * свертки (левая и правая);
    * структура должна быть моноидом.
2. Структуры данных должны быть неизменяемыми.
3. Библиотека должна быть протестирована в рамках unit testing.
4. Библиотека должна быть протестирована в рамках property-based тестирования (как минимум 3 свойства, включая свойства
   моноида).
5. Структура должна быть полиморфной.
6. Требуется использовать идиоматичный для технологии стиль программирования. Примечание: некоторые языки позволяют
   получить большую часть API через реализацию небольшого интерфейса. Так как лабораторная работа про ФП, а не про
   экосистему языка -- необходимо реализовать их вручную и по возможности -- обеспечить совместимость.

## Описание реализации

Объявление протокола структуры данных:

```clojure
(defprotocol IRedBlackTreeSet
  (insert [this x])
  (delete [this x])
  (filter-values [this pred])
  (map-values [this f])
  (fold-left-values [this init f])
  (fold-right-values [this init f])
  (combine [this other]))
```

Реализация структуры данных, согласно протоколу:

```clojure
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
```

Функция для создания структуры данных:

```clojure
(defn rb-set [& args]
  (reduce #(insert %1 %2) (RedBlackTreeSet. nil) args))
```

## Разработанное тестовое покрытие

### Unit тесты

```clojure
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
```

### Property-based тесты

```clojure
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
```

## Заключение

В ходе выполнения лабораторной работы были реализованы основные функции для работы с красно-черным деревом. Структура
данных была протестирована с использованием unit и property-based тестов. Реализация показала, что использование
неизменяемых структур позволяет легко сохранять целостность данных и упрощает тестирование.