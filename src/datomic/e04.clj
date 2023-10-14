(ns datomic.e04)

(require '[datomic.client.api :as d])
(use '[datomic.e03 :only [conn] :as e03])

; e03'ün devamı

;Konu: Identity erişimi

; Yukarıdaki sorgularda "Kırmızı kalem" siparişlerine ulaşmak için iki tane data pattern yazmamız gerekti.
; Bu çok pratik bir yöntem değil.
; Normalde biz alıştığımız veritabanı sorgularında bir tane FK referansıyla bunu hallederiz.
; Benzeri bir yaklaşımı burada nasıl uygularız?

; Bir tane identity PK kolonu tanımlarız
(def product-schema-2
  [{:db/ident :product/id
    :db/valueType :db.type/long
    :db/unique :db.unique/identity ; bu spesifikasyonla (spek) bu atributu PK kolonu haline getirdik
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data product-schema-2})

(def db (d/db conn))

(d/q '[:find (pull ?e [*])
       :where
       [?e :product/name _]]
     db)

;=>
;[[{:db/id 83562883711062, :product/name "Kalem", :product/color #:db{:id 87960930222156, :ident :color/red}}]
; [{:db/id 83562883711063, :product/name "Kalem", :product/color #:db{:id 87960930222158, :ident :color/blue}}]
; [{:db/id 83562883711064, :product/name "Defter", :product/color #:db{:id 87960930222156, :ident :color/red}}]
; [{:db/id 83562883711065, :product/name "Defter", :product/color #:db{:id 87960930222157, :ident :color/green}}]
; [{:db/id 87960930222162, :product/name "Kalem", :product/color #:db{:id 87960930222156, :ident :color/red}}]
; [{:db/id 87960930222163, :product/name "Kalem", :product/color #:db{:id 87960930222158, :ident :color/blue}}]
; [{:db/id 87960930222164, :product/name "Defter", :product/color #:db{:id 87960930222156, :ident :color/red}}]
; [{:db/id 87960930222165, :product/name "Defter", :product/color #:db{:id 87960930222157, :ident :color/green}}]]


; Dikkat: Başka varlıklara verilen referanslar, diğer atributlardan farklı olarak bir kv ikilisi (key-value pair) olarak görünüyor:
; 👉 :product/color #:db{:id 96757023244361, :ident :color/red} 👈
; Diğer normal atributların değerleri ise primitif olarak geliyor:
; :product/name "Kalem"

; Terim: primitif (primitive), obje (aggregate, alt parçası olan objeler, composite, map gibi)

; Bu ürün listesinde içiçe iki seviye (nested) vektör bulunuyor. [[...][...]]
; Tek seviye olmasını sağlamak için, flatten fonksiyonunu kullanalım
  ; Terim: flatten = düzleştirme. Çok seviyeli (nested, içiçe) olan vektör, liste türü koleksiyonları, tek seviyeye indirmek.



(-> (d/q
      '[:find (pull ?e [*])
        :where [?e :product/name _]]
      db) flatten)

;=>
;({:db/id 83562883711062, :product/name "Kalem", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 83562883711063, :product/name "Kalem", :product/color #:db{:id 87960930222158, :ident :color/blue}}
; {:db/id 83562883711064, :product/name "Defter", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 83562883711065, :product/name "Defter", :product/color #:db{:id 87960930222157, :ident :color/green}}
; {:db/id 87960930222162, :product/name "Kalem", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 87960930222163, :product/name "Kalem", :product/color #:db{:id 87960930222158, :ident :color/blue}}
; {:db/id 87960930222164, :product/name "Defter", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 87960930222165, :product/name "Defter", :product/color #:db{:id 87960930222157, :ident :color/green}})

; Şimdi tek seviyelik liste oldu. Ancak bizim liste değil vektör olmasına ihtiyacımız var, kolayca assoc-in ile öğeleri dolaşabilmek için

(-> (d/q '[:find (pull ?e [*])
           :where
           [?e :product/name _]]
         db)
    flatten
    vec)

;=>
;[{:db/id 83562883711062, :product/name "Kalem", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 83562883711063, :product/name "Kalem", :product/color #:db{:id 87960930222158, :ident :color/blue}}
; {:db/id 83562883711064, :product/name "Defter", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 83562883711065, :product/name "Defter", :product/color #:db{:id 87960930222157, :ident :color/green}}
; {:db/id 87960930222162, :product/name "Kalem", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 87960930222163, :product/name "Kalem", :product/color #:db{:id 87960930222158, :ident :color/blue}}
; {:db/id 87960930222164, :product/name "Defter", :product/color #:db{:id 87960930222156, :ident :color/red}}
; {:db/id 87960930222165, :product/name "Defter", :product/color #:db{:id 87960930222157, :ident :color/green}}]

(def product-list-3
  (-> (d/q
        '[:find (pull ?e [*])
          :where
          [?e :product/name _]]
        db)
      flatten
      vec))

; Şimdi bu ürün listesindeki her bir ürün öğesine bir product/id ekleyelim

(def product-list-4
  (-> product-list-3
      (assoc-in [0 :product/id] 1)
      (assoc-in [1 :product/id] 2)
      (assoc-in [2 :product/id] 3)
      (assoc-in [3 :product/id] 4)))


(identity product-list-4)

;=>
;[{:db/id 83562883711062,
;  :product/name "Kalem",
;  :product/color #:db{:id 87960930222156, :ident :color/red},
;  :product/id 1}
; {:db/id 83562883711063,
;  :product/name "Kalem",
;  :product/color #:db{:id 87960930222158, :ident :color/blue},
;  :product/id 2}
; {:db/id 83562883711064,
;  :product/name "Defter",
;  :product/color #:db{:id 87960930222156, :ident :color/red},
;  :product/id 3}
; {:db/id 83562883711065,
;  :product/name "Defter",
;  :product/color #:db{:id 87960930222157, :ident :color/green},
;  :product/id 4}
; {:db/id 87960930222162,
;  :product/name "Kalem",
;  :product/color #:db{:id 87960930222156, :ident :color/red},
;  :product/id 5}
; {:db/id 87960930222163,
;  :product/name "Kalem",
;  :product/color #:db{:id 87960930222158, :ident :color/blue},
;  :product/id 6}
; {:db/id 87960930222164,
;  :product/name "Defter",
;  :product/color #:db{:id 87960930222156, :ident :color/red},
;  :product/id 7}
; {:db/id 87960930222165,
;  :product/name "Defter",
;  :product/color #:db{:id 87960930222157, :ident :color/green},
;  :product/id 8}]

(d/transact conn {:tx-data product-list-4})

(def db (d/db conn))

(d/q
  '[:find (pull ?e [*])
    :where
    [?e :product/name _]]
  db)

; Not: (d/transact conn {:tx-data product-list-4}) ifadesiyle güncel ürün listesini veritabanına kaydetmiştik.
; Dikkat ederseniz, product-list-4 içinde :product/id dışında 3 atribut daha bulunuyordu.
; Peki datomic neden bu 4 varlık için, yeni entity kayıtları oluşturmak yerine, mevcut kayıtları güncelledi?
; Cevap: Çünkü :db/id ile mevcut varlıkların id'lerini vermiştik.
; Datomic bu db/id değerlerine ait varlıkları buldu veritabanından. Sonra bunların atribut değerlerini güncelledi.
; Yeni varlık kaydı eklemedi.

; Şimdi yeni bir sipariş daha verelim.
; Daha önceki sipariş tanımlama kodumuz şu şekildeydi:
;(def order-list
;  [{:order/product product-id
;    :order/size 6}])
; Bu kodun sorunu `product-id` isimli entity_id değerini bulmak için bir hayli ek iş yapmak gerekiyordu.
; Artık her bir ürün kaydımız için tekil bir PK atributumuz (:product/id) bulunduğundan, işimiz çok daha kolay.

(def order-list-3
  [{:order/product [:product/id 2]
    :order/size 7}])

(d/transact conn {:tx-data order-list-3})







