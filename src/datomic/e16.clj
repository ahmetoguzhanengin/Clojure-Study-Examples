(ns datomic.e16)

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])

(require '[datomic.e04 :as e04])

(def db (d/db conn))

; Datomice veri kaydetmenin iki farklı yolu var.
; Şu ana kadar hep düz map objelerini kaydetmiştik.
; Örneğin:

(def product-list
  [{:product/id 5
    :product/name "Silgi"
    :product/color :color/blue}])

(d/transact conn {:tx-data product-list})

(def product-list-2
  [[:db/add "foo" :product/id 6]
   [:db/add "foo" :product/name "Boya"]
   [:db/add "foo" :product/color :color/red]])

(d/transact conn {:tx-data product-list-2})

(def db (d/db conn))

(d/q
  '[:find (pull ?product [*])
    :where
    [?product :product/id]]
  db)

;=>
;[[{:db/id 83562883711067,
;   :product/name "Boya",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 6}]
; [{:db/id 87960930222170,
;   :product/name "Silgi",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 5}]
; [{:db/id 92358976733266,
;   :product/name "Kalem",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 1}]
; [{:db/id 92358976733267,
;   :product/name "Kalem",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 2}]
; [{:db/id 92358976733268,
;   :product/name "Defter",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 3}]
; [{:db/id 92358976733269,
;   :product/name "Defter",
;   :product/color #:db{:id 92358976733261, :ident :color/green},
;   :product/id 4}]]

; db/retract
; Eğer herhangi bir atributun değerini değiştirmek veya iptal etmek istiyorsak da iki yol var:
; Yeni düzeltilmiş veriyle objeyi tekrar kaydetmek

(def product-list-3
  [{:product/id 5
    :product/name "Silgili Kalem"
    :product/color :color/blue}])

(d/transact conn {:tx-data product-list-3})

(def db (d/db conn))

(d/q
  '[:find (pull ?product [*])
    :where
    [?product :product/id]]
  db)

; [{:db/id 87960930222170,
;   :product/name "Silgili Kalem",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 5}]

; Başka bir yol da :db/retract adı verilen transaction function kullanmaktır.

(def product-list-4
  [[:db/retract [:product/id 6] :product/name "Boya"]
   [:db/add "datomic.tx" :db/doc "Ürün ismi yanlış girilmiş"]])

(d/transact conn {:tx-data product-list-4})

(def db (d/db conn))

(d/q
  '[:find (pull ?product [*])
    :where
    [?product :product/id]]
  db)

;=>
;[[{:db/id 83562883711067, :product/color #:db{:id 92358976733260, :ident :color/red}, :product/id 6}]
; [{:db/id 87960930222170,
;   :product/name "Silgili Kalem",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 5}]
; [{:db/id 92358976733266,
;   :product/name "Kalem",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 1}]
; [{:db/id 92358976733267,
;   :product/name "Kalem",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 2}]
; [{:db/id 92358976733268,
;   :product/name "Defter",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 3}]
; [{:db/id 92358976733269,
;   :product/name "Defter",
;   :product/color #:db{:id 92358976733261, :ident :color/green},
;   :product/id 4}]]


; Yukarıdaki örnekte :db/retract yapmak yerine doğrudan doğru veriyi yazarak da eski veriyi temizleyebiliriz
(def product-list-5
  [[:db/add [:product/id 6] :product/name "Pastel Boya"]
   [:db/add "datomic.tx" :db/doc "Ürün ismi yanlış girilmiş"]])
(d/transact conn {:tx-data product-list-5})

(def db (d/db conn))

(d/q
  '[:find (pull ?product [*])
    :where
    [?product :product/id]]
  db)

;=>
;[[{:db/id 83562883711067,
;   :product/name "Pastel Boya",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 6}]
; [{:db/id 87960930222170,
;   :product/name "Silgili Kalem",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 5}]
; [{:db/id 92358976733266,
;   :product/name "Kalem",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 1}]
; [{:db/id 92358976733267,
;   :product/name "Kalem",
;   :product/color #:db{:id 92358976733262, :ident :color/blue},
;   :product/id 2}]
; [{:db/id 92358976733268,
;   :product/name "Defter",
;   :product/color #:db{:id 92358976733260, :ident :color/red},
;   :product/id 3}]
; [{:db/id 92358976733269,
;   :product/name "Defter",
;   :product/color #:db{:id 92358976733261, :ident :color/green},
;   :product/id 4}]]

; [:db/add ...] ile kullanıma list formu
; {..} ile veri kaydetmeye map formu deniyor