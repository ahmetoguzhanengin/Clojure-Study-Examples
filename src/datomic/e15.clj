(ns datomic.e15)

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])

(require '[datomic.e04 :as e04])

(def db (d/db conn))

; Konu: Reverse Lookup (Ters Referans)

; Forward navigation

; Siparişten Ürüne ulaşabiliyoruz :order/product referansıyla

(d/q
  '[:find (pull ?order [:db/id {:order/product [:product/name]}])
    :where
    [?order :order/product ?e]]
  db)

;=>
;[[{:db/id 74766790688856, :order/product #:product{:name "Kalem"}}]
; [{:db/id 87960930222169, :order/product #:product{:name "Kalem"}}]
; [#:db{:id 101155069755478}]
; [#:db{:id 101155069755479}]]

; Sipariş varlıklarının içinde :order/product atributu bulunuyor. Bu da ilgili Product varlığına ref içeriyor.
; Peki bunun tam tersi yapılamaz mı?
; Yani elimde bir Product varken, buna referans veren Order objesine erişebilir miyim pull api içinden?

; Teorik olarak mümkün olmalı.
; Ancak bu özellik normalde bildiğimiz programlama dillerinin map interfacelerinde bulunmaz.
; Datomic şöyle bir sentaks getirir: :order/_product dedin mi, ters yönde referans olarak kabul eder bunu.

(d/q
  '[:find (pull ?product [*])
    :where
    [?product :product/id]]
  db)
;=>
;[[{:db/id 92358976733266,
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

; Bu şekilde veritabanımızdaki tüm Product kayıtlarını çektik.
; Şimdi bu Product objelerinin her biri hangi siparişlerde bulunuyorsa, onları da görmek istiyoruz.

(d/q
  '[:find (pull ?product [:product/id :product/name :order/_product])
    :where
    [?product :product/id]]
  db)

;=>
;[[{:product/id 1, :product/name "Kalem", :order/_product [#:db{:id 74766790688856}]}]
; [{:product/id 2, :product/name "Kalem", :order/_product [#:db{:id 87960930222169}]}]
; [#:product{:id 3, :name "Defter"}]
; [#:product{:id 4, :name "Defter"}]]

; İlginç nokta şu:
;; Aslında Product'tan Order'a referans yok.
;; Ama sanki varmış gibi :order/product referansını tersten kullandık

;; implementasyon tarafında pull api kullanmasak doğrudan data patternlarla da yapabilirdik

(d/q
  '[:find ?product ?order
    :where
    [?product :product/id ?pid]
    [?order :order/product ?product]]
  db)
;=> [[92358976733266 74766790688856]
;    [92358976733267 87960930222169]]

; Bu formatta sorgulayınca her bir Product'ın içinde bulunduğu Siparişi getiriyor
; Ama flat (tabular) formatta getirdi
; İlkinde pull API'daysa nested map formatta getirmişti.

; Mesela her ürünün içinde bulunduğu siparişlerin miktarlarını bulalım

(d/q
  '[:find (pull ?e [:product/id {:order/_product [:order/size]}])
    :where
    [?e :product/name]]
  db)

;=>
;[[{:product/id 1, :order/_product [#:order{:size 6}]}]
; [{:product/id 2, :order/_product [#:order{:size 7}]}]
; [#:product{:id 3}]
; [#:product{:id 4}]]




