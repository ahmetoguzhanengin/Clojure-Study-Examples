(ns ex02)
;;Tarih 20230930


(def m1 {:k1 :v1 :k2 :v2})

(def m2 {:k1 :v3 :k2 :v4})

(def Ms [m1 m2])

(println Ms)

(get Ms 0)

(println (get Ms 0))

(get Ms 1)

(println (get Ms 1))

(->
  (get Ms 0)
  (get :k1))

(println (->
           (get Ms 0)
           (get :k1))
         )

(get (get Ms 0) :k1)

(:k1 m1)

(println (:k1 m1))

(:k1 {:k1 :v1 , :k2 :v2})

(println (:k1 {:k1 :v1 , :k2 :v2}))

(m1 :k1)

(println (m1 :k1))

(-> Ms
    (get 0)
    (:k1)
    )

(println (-> Ms
             (get 0)
             (:k1)
             )
         )

(get-in Ms [0 :k1])
(get-in Ms [1 :k1])

(println (get-in Ms [0 :k1]))
(println (get-in Ms [1 :k1]))

(filter
  (fn [v] (= 3 v)) [3 5 7]
  )

(filter
  (fn [p] (:k1 p))
  {:k1 1 :k2 2})
;;=> ()

(filter
  #(:k1 %)
  {:k1 1 :k2 2})

(filter
  #(= (:k1 %) 1)
  {:k1 1 :k2 2})
;=> ()

(filter
  (fn [[k v]] (= v 1))
  {:k1 1 :k2 2}
  )

(->> Ms
     (filter
       (fn [m] (= (:k1 m) :v1))))


(filter
  (fn [m] (= (get-in m [:k1]) :v1))
  Ms)
