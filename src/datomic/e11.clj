(ns datomic.e11)

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])

(require '[datomic.e04 :as e04])

(def db (d/db conn))

; Bu konu, predicatelara çok benziyor.
; Predicatelarda biz sorgu kriterlerimizin içinde fonksiyon kullanıyorduk.
; Transformasyon fonksiyonlarındaysa, yine data pattern içinde kullanacağız bu fonksiyonları
; Ama amacımız bu sefer ?x mantıksal değişkenlerimizin içindeki verileri işlemek (transformasyon)

; Mesela hepimiz çarpma işlemini biliriz
; Doğrudan dahili `*` operatörünü kullanalım şimdi

(d/q
  '[:find ?order ?size ?result
    :where
    [?order :order/size ?size]
    [(* ?size 10) ?result]]
  db)

;=> [[83562883711064 6 60] [79164837199958 5 50] [92358976733273 7 70] [79164837199959 4 40]]

; Eğer kullandığımız fonksiyon clojure.core ns'inin bir üyesiyse, direk ismiyle kullanabiliriz
; Eğer başka bir ns'den geliyorsa, o zaman uzun ismiyle kullanmalıyız.

; Aynı predicatelarda olduğu gibi kendi fonksiyonlarımızı da tanımlayabiliriz
; Saf olması şartı burada da geçerli.
; Uzun isimle kullanmamız gerekiyor

(defn multiply_by [factor1 factor2]
  (* factor1 factor2))

(d/q
  '[:find ?order ?size ?result
    :where
    [?order :order/size ?size]
    [(datomic.e11/multiply_by ?size 10) ?result]]
  db)
;=> [[83562883711064 6 60] [79164837199958 5 50] [92358976733273 7 70] [79164837199959 4 40]]
; Dikkat: `multiply_by` fonksiyonunun sonucunu ?result değişkeninin içine koyduk

;| Binding Form | Binds      |
;|--------------|------------|
;| ?a           | scalar     |
;| [?a ?b]      | tuple      |
;| [?a …]       | collection |
;| [ [?a ?b ] ] | relation   |

; Burada transformasyon fonksiyonu scalar (primitif) bir değer döndü.
; Eğer transformasyon fonksiyonunun sonucu tuple, collection, veya relation ise o zaman binding yapmamız gerekir.

; Tuple Bağlama (Binding):

(defn to_tuple [factor1 factor2]
  [(* factor1 factor2) (+ factor1 factor2)])

(to_tuple 4 5)
;=> [20 9]

(d/q
  '[:find ?order ?size ?a1 ?a2
    :where
    [?order :order/size ?size]
    [(datomic.e11/to_tuple ?size 10) [?a1 ?a2]]]
  db)

;=> [[83562883711064 6 60 16] [79164837199959 4 40 14] [79164837199958 5 50 15] [92358976733273 7 70 17]]

; Collection Bağlama (Binding):

(defn to_coll [arg]
  (range arg))

(to_coll 7)

(d/q
  '[:find ?order ?size ?xs
    :where
    [?order :order/size ?size]
    [(< ?size 5)]
    [(datomic.e11/to_coll ?size) [?xs ...]]]
  db)
;=> [[79164837199959 4 0] [79164837199959 4 1] [79164837199959 4 2] [79164837199959 4 3]]
; to_coll (0 1 2 3) listesini dönüyor
; ?order ve ?size için  [[79164837199959 4]] değerlerini bağlamıştı
;şimdi bu iki kümenin bir cross joinini alıyoruz. Bir tane order objesiyle 4 tane rakamı kombine edeiyoruz.
; Relation bağlamadan önce, parametrik sorguda relation nasıl bağlanıyordu hatırlayalım

(d/q
  '[:find ?e ?product-name ?color ?product-price
    :in $ [[?product-name ?product-price]]
    :where
    [?e :product/name ?product-name]
    [?e :product/color ?color]]
  db [["Kalem" 120] ["Defter" 250]])

;=>
;[[87960930222163 "Kalem" 92358976733262 120]
; [87960930222164 "Defter" 92358976733260 250]
; [87960930222165 "Defter" 92358976733261 250]
; [87960930222162 "Kalem" 92358976733260 120]]

; Relation Bağlama (Binding):

; Önce verilen bir sayı için bir relation dönen bir fonksiyon tanımlayalım

(defn to_rel [arg]
  (take
    (mod arg 7)
    [[:a 1] [:b 2] [:c 3] [:d 4] [:e 5] [:f 6]]))
(to_rel 3)

;=> ([:a 1]
;    [:b 2]
;    [:c 3])
; Bunu bir tablo gibi düşünebiliriz.
; İlk kolon :a :b :c listesinden oluşuyor
; İkinci kolonsa 1 2 3 değerlerinden oluşuyor

(d/q
  '[:find ?order ?size ?a ?b
    :where
    [?order :order/size ?size]
    [(< ?size 5)]
    [(datomic.e11/to_rel ?size) [[?a ?b]]]]
  db)





