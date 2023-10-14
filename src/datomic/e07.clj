(ns datomic.e07)

; Konu: Parametrik Sorgularda Destructuring ve Binding

; Source: [Learn Datalog Today!](https://www.learndatalogtoday.org/chapter/3)

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])
(use '[datomic.e04 :as e04])

(d/q
  '[:find ?e
    :in $ ?product-name
    :where
    [$ ?e :product/name ?product-name]]
  db "Kalem")

;=> [[96757023244370] [96757023244371]]
; Bu güzel
; Burada parametre olarak gönderdiğimiz değer primitif bir değer.
; Fakat bizim clojureda fonksiyonlara sadece primitif tipte değerler değil, kompozit (bileşik) türde değerler de gönderebiliyoruz.
; Tuples/Vector/List: [.. ..]
; Map
; Bunların bir kombinasyonu olabilir

; Bu tarz bir değer argüman olarak gönderileceği vakit destructuring yapılması gerekiyor.
; Klasik clojure destructuring sentaksı gibi, ama biraz farklılıklar var.
; [Clojure - Destructuring in Clojure](https://clojure.org/guides/destructuring)
; [Clojure Destructuring Tutorial and Cheat Sheet](https://gist.github.com/john2x/e1dca953548bfdfb9844)

; Tuple:

(d/q
  '[:find ?e
    :in $ [?product-name ?color]
    :where
    [?e :product/name ?product-name]
    [?e :product/color ?color]]
     db ["Kalem" :color/red])

; Yani hem Kalem hem de kırmızı renkli olan varlıkları sorguladık

; Collection:

(d/q
  '[:find ?e
    :in $ [?product-name ...] ;... sembolü
    :where
    [?e :product/name ?product-name]]
  db ["Kalem" "Defter"]
  )

;=> [[96757023244370] [96757023244371] [96757023244372] [96757023244373]]
; Kalem veya Defter olan varlıkları sorgulamış olduk

(d/q
  '[:find ?e
    :in $ [?color ...]
    :where
    [?e :product/color ?color]]
  db [:color/red :color/blue :color/green])

;=> [[96757023244370] [96757023244371] [96757023244372] [96757023244373]]

(d/q '[:find ?e ?product-name ?color ?product-price
       :in $ [[?product-name ?product-price]]
       :where
       [?e :product/name ?product-name]
       [?e :product/color ?color]]
     db [["Kalem" 120] ["Defter" 250]])

;=>
;[[74766790688851 "Kalem" 87960930222158 120]
; [74766790688853 "Defter" 87960930222157 250]
; [74766790688850 "Kalem" 87960930222156 120]
; [74766790688852 "Defter" 87960930222156 250]]

; Burada veritabanımızda olmayan yeni bir bilgiyi (product-price) sorgu sonuçlarımıza otomatikman join ettik.
; join external relations
; relation: kelimesini görünce aklınıza tablo gelsin.

; Bindings
; Şimdi bu parametrelerin sorgu değişkenlerine bağlanması işine binding diyoruz.


;| Binding Form | Binds      |
;|--------------|------------|
;| ?a           | scalar     |
;| [?a ?b]      | tuple      |
;| [?a …]       | collection |
;| [ [?a ?b ] ] | relation   |


; İlk yaptığımız bağlama scalardı: ?a

(d/q
  '[:find ?e
    :in $ ?product-name
    :where
    [$ ?e :product/name ?product-name]]
  db "Kalem")

;=> [[74766790688850] [74766790688851]]

; Tuple: [?a ?b]
(d/q
  '[:find ?e
    :in $ [?product-name ?color]
    :where
    [?e :product/name ?product-name]
    [?e :product/color ?color]]
  db ["Kalem" :color/red])
;=> [[74766790688850]]

; Collection: [?a ...]
(d/q
  '[:find ?e
    :in $ [?product-name ...]
    :where
    [?e :product/name ?product-name]]
  db ["Kalem" "Defter"])
;=> [[74766790688850] [74766790688851] [74766790688852] [74766790688853]]

; Relation: [ [?a ?b] ]

(d/q
  '[:find ?e ?product-name ?color ?product-price
    :in $ [[?product-name ?product-price]]
    :where
    [?e :product/name ?product-name]
    [?e :product/color ?color]]
   db [["Kalem" 120]
       ["Defter" 250]])
;=>
;[[74766790688851 "Kalem" 87960930222158 120]
; [74766790688853 "Defter" 87960930222157 250]
; [74766790688850 "Kalem" 87960930222156 120]
; [74766790688852 "Defter" 87960930222156 250]]