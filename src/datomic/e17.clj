(ns datomic.e17
  (:require [datascript.core :as d]))
(def conn (d/create-conn))

; Konu: DataScript


(def datoms [{:db/id -1 :name "Bob" :age 30}
             {:db/id -2 :name "Sally" :age 15}])

(d/transact! conn datoms)

;18 yaşından küçük insanların ismi
(def q-young '[:find ?n
               :in $ ?min-age
               :where
               [?e :name ?n]
               [?e :age ?a]
               [(< ?a ?min-age)]])

(d/q q-young @conn 18)

;=> #{["Sally"]}
(def product-list
  [{:product/id 5
    :product/name "Silgi"
    :product/color :color/blue}])

(d/transact! conn product-list)

; Datomic'te herhangi bir entity'yi kaydetmeden önce, o entitynin schemasını tanımlayıp kaydetmemiz gerekiyordu.
; Fakat DataScript'te veritabanı otomatikman verdiğin entity'nin atributlarına bakarak, schemayı kendisi oluşturur.
; Birkaç satır daha veri ekleyelim

(def product-list
  [{:product/id 1
    :product/name "Kalem"
    :product/color :color/red}
   {:product/id 2
    :product/name "Kalem"
    :product/color :color/blue}
   {:product/id 3
    :product/name "Defter"
    :product/color :color/red}
   {:product/id 4
    :product/name "Defter"
    :product/color :color/green}])

(d/transact! conn product-list)

(def product-list-2
  [[:db/add "foo" :product/id 6]
   [:db/add "foo" :product/name "Boya"]
   [:db/add "foo" :product/color :color/red]])

(d/transact! conn product-list-2)

; Transaction fonksiyonlarıyla da veri ekleyebiliyoruz.

; Datomic ve DataScript iki farklı library (package)
; Fakat interfaceleri birbiriyle uyumlu.
; Ama ufak tefek farklılıklar da var. Mesela transact fonksiyonunda:
;(d/transact! conn product-list-2)  👈 DataScript
;(d/transact conn {:tx-data product-list-3})   👈 Datomic

; Sorgulamayı deneyelim

(d/q
  '[:find ?product
    :where
    [?product :product/id]]
  @conn)

;=> #{[4] [7] [6] [3] [8] [5]}

; Parametrik sorgulamayı deneyelim
(d/q
  '[:find ?e
    :in $ ?product-name
    :where
    [?e :product/name ?product-name]]
  @conn "Kalem")

;=> #{[4] [5]}

; Pull API deneyelim

(d/q
  '[:find (pull ?product [*])
    :where
    [?product :product/id]]
  @conn)
;=>
;([{:db/id 4, :product/color :color/red, :product/id 1, :product/name "Kalem"}]
; [{:db/id 7, :product/color :color/green, :product/id 4, :product/name "Defter"}]
; [{:db/id 6, :product/color :color/red, :product/id 3, :product/name "Defter"}]
; [{:db/id 3, :product/color :color/blue, :product/id 5, :product/name "Silgi"}]
; [{:db/id 8, :product/color :color/red, :product/id 6, :product/name "Boya"}]
; [{:db/id 5, :product/color :color/blue, :product/id 2, :product/name "Kalem"}])

; Bir de ref objeleriyle kayıt yapalım

(def order-list
  [{:order/product [:product/id 2] :order/size 6}
   {:order/product [:product/id 1] :order/size 3}
   {:order/product [:product/id 3] :order/size 5}])

(d/transact! conn order-list)

; Şimdi Pull API ile ref objelere erişip nested bir map dönelim

(d/q
  '[:find (pull ?order [*])
    :where
    [?order :order/product _]]
  @conn)

;=>
;([{:db/id 10, :order/product [:product/id 1], :order/size 3}]
; [{:db/id 9, :order/product [:product/id 2], :order/size 6}]
; [{:db/id 11, :order/product [:product/id 3], :order/size 5}])



#_(d/q
    '[:find (pull ?order [*])
      :where
      [?order :order/product ?product]
      [?product :product/id ?pid]]
    @conn)
; Hata verdi
; Lookup ref attribute should be marked as :db/unique: [:product/id 2]
; unique olan kolonları schema ile açıkça belirtmemiz gerekiyor.

(def product-schema
  [{:product/id {:db/valueType :db.type/long
                 :db/unique :db.unique/identity
                 :db/cardinality :db.cardinality/one}}])
(d/transact! conn product-schema)

(def conn (d/create-conn))
(d/q
  '[:find (pull ?order [*])
    :where
    [?order :order/product ?product]
    [?product :product/id ?pid]]
  @conn)
; TODO hata: Lookup ref attribute should be marked as :db/unique: [:product/id 2]

