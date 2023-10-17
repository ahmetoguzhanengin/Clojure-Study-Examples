(ns datomic.e12)

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])

(require '[datomic.e04 :as e04])

(def db (d/db conn))

; Konu: Aggregates

; SQL'deki MAX, MIN, DISTINCT, AVG gibi fonksiyonlara aggregate fonksiyonları deniyor.

; Örnek: Tüm siparişlerin adet miktarlarının ortalamasını bulmak istiyoruz:
; Normalde tüm siparişlerin adetlerini bir çıkartalım

(d/q
  '[:find ?order ?size
    :where
    [?order :order/size ?size]]
  db)
;=> [[92358976733273 7] [79164837199959 4] [83562883711064 6] [79164837199958 5]]
(d/q
  '[:find ?size
    :where
    [?order :order/size ?size]]
  db)
;=> [[4] [5] [6] [7]]

(d/q
  '[:find (distinct ?size)
    :where
    [?order :order/size ?size]]
  db)
;=> [[#{7 4 6 5}]]

; distinct: ayrık bir şekilde sonuçları listeler. tekrarlanan değerleri birleştirir

(d/q
  '[:find (count ?size)
    :where
    [?order :order/size ?size]]
  db)
;=> [[4]]

(d/q
  '[:find (count ?order)
    :where
    [?order :order/size ?size]]
  db)

;=> [[4]]
(d/q
  '[:find (max ?size)
    :where
    [?order :order/size ?size]]
  db)

;=> [[7]]

; (min ?xs)   en küçük değeri (numerik)
; (max ?xs)   en büyük değeri
; (count ?xs)    kaç tane adet olduğunu verir
; (count-distinct ?xs)   kaç farklı adet olduğunu verir
; (sum ?xs)      toplamını hesaplar
; (avg ?xs)      ortalamasını hesaplar (average)
; (median ?xs)   ortanca değeri verir
; (variance ?xs)   istatistiksel varyansı
; (stddev ?xs)     istatistiksel standart sapmayı verir


; https://docs.datomic.com/on-prem/query/query.html#aggregates-returning-a-single-value
; (min ?xs)   en küçük değeri (numerik)
; (max ?xs)   en büyük değeri
; (count ?xs)    kaç tane adet olduğunu verir
; (count-distinct ?xs)   kaç farklı adet olduğunu verir
; (sum ?xs)      toplamını hesaplar
; (avg ?xs)      ortalamasını hesaplar (average)
; (median ?xs)   ortanca değeri verir
; (variance ?xs)   istatistiksel varyansı
; (stddev ?xs)     istatistiksel standart sapmayı verir

; Aggregate kelimesinin anlamı nedir?
; Aggregate, yığın, küme, birleştirmek gibi anlamlarda kullanılan bir kelime.
; Birden çok şeyi toparlamak veya gruplamak gibi bir anlamda düşünülebilir.
; Veri sorgularımızda dönen sonuç kümesindeki tek tek satırları döndürmek yerine,
; bu kümenin tümüne veya alt gruplarına dair bir istatistik üretmek için kullanıyoruz.

; Bu yukarıdaki örnekler, tek bir değer (scalar) dönen aggregate fonksiyonları.
; Bir de koleksiyon dönen aggregate fonksiyonları var.

; https://docs.datomic.com/on-prem/query/query.html#aggregates-returning-collections
; (distinct ?xs)     ayrık öğeleri döner
; (min n ?xs)        min n öğeyi verir
; (max n ?xs)        en büyük n öğeyi verir
; (rand n ?xs)
; (sample n ?xs)     verdiğin kümeden n tane örnek alır



(d/q
  '[:find (max 3 ?size)
    :where
    [?order :order/size ?size]]
  db)
;=> [[[7 6 5]]]

(d/q
  '[:find (sample 3 ?order)
    :where
    [?order :order/size ?size]]
  db)

=> [[[79164837199959 79164837199958 92358976733273]]]

; Custom Aggregates

; Kendi özel aggregate fonksiyonumuzu yazmak da Predicate veya Transformation Fonksiyonlarıyla benzer şekilde.
; mode fonksiyonu bir listedeki en çok kullanılan öğeyi dönsün.

(defn mode
  [vals]
  (->> (frequencies vals)
       (sort-by (comp - second))
       ffirst))
(mode '(10 10 10 20 30 20))
;=> 10
(mode '(1 2 2 2 2 3))
;=> 2
(mode '(1 2 3 3 2 3))
;=> 3

; Bu mode fonksiyonuyla dönen bir result set (bir sorgunun sonucunda dönen küme) içindeki en çok tekrarlanan öğeyi döner.

(def order-list-5
  [{:order/product [:product/id 2] :order/size 4}
   {:order/product [:product/id 3] :order/size 5}
   {:order/product [:product/id 1] :order/size 4}
   {:order/product [:product/id 2] :order/size 3}])

(d/transact conn {:tx-data order-list-5})

(def db (d/db conn))
(d/q
  '[:find (pull ?order [*])
    :where
    [?order :order/size ?size]]
  db)
;=>
;[[{:db/id 79164837199958, :order/product #:db{:id 74766790688850}, :order/size 5}]
; [{:db/id 79164837199959, :order/product #:db{:id 74766790688852}, :order/size 4}]
; [{:db/id 83562883711064, :order/product #:db{:id 87960930222162}, :order/size 6}]
; [{:db/id 92358976733273, :order/product #:db{:id 87960930222163}, :order/size 7}]
; [{:db/id 101155069755482, :order/product #:db{:id 87960930222163}, :order/size 4}]
; [{:db/id 101155069755483, :order/product #:db{:id 87960930222164}, :order/size 5}]
; [{:db/id 101155069755484, :order/product #:db{:id 87960930222162}, :order/size 4}]
; [{:db/id 101155069755485, :order/product #:db{:id 87960930222163}, :order/size 3}]]



(d/q
  '[:find ?order ?size
    :where
    [?order :order/size ?size]]
  db)
;=>
;[[101155069755482 4]
; [101155069755483 5]
; [101155069755484 4]
; [92358976733273 7]
; [79164837199959 4]
; [83562883711064 6]
; [79164837199958 5]
; [101155069755485 3]]


(d/q
  '[:find (datomic.e12/mode ?size)
    :where
    [?order :order/size ?size]]
  db)




(d/q
  '[:find ?order ?size
    :where
    [?order :order/size ?size]]
  db)
;=>
;[[101155069755482 4]
; [101155069755483 5]
; [101155069755484 4]
; [92358976733273 7]
; [79164837199959 4]
; [83562883711064 6]
; [79164837199958 5]
; [101155069755485 3]]
(map second (d/q
              '[:find ?order ?size
                :where
                [?order :order/size ?size]]
              db))
;=> (4 5 4 7 4 6 5 3)

(mode
  (map second (d/q
                '[:find ?order ?size
                  :where
                  [?order :order/size ?size]]
                db)))
;=> 4

;aynı ifade
(->>
  (d/q
    '[:find ?order ?size
      :where
      [?order :order/size ?size]]
    db)
  (map second)
  (mode))

;GROUP BY
; https://docs.datomic.com/cloud/query/query-data-reference.html#aggregate-example

; SQL'de bu aggregate fonksiyonları genelde her zaman GROUP BY ile birlikte kullanılır
; datomicde `GROUP BY` ifadesini kullanmayız. farklı bir formatta bunu yaparız.

; [:find ?a (min ?b) (max ?b) ?c (sample 12 ?d)
; ?a ve ?c'ye göre gruplar

(d/q
  '[:find ?size (count ?size)
    :where
    [?order :order/size ?size]]
  db)
;=> [[3 1] [4 1] [5 1] [6 1] [7 1]]

(map second (d/q
              '[:find ?order ?size
                :where
                [?order :order/size ?size]]
              db))
;=> (4 5 2 3 4 7 6 5 3 4)
