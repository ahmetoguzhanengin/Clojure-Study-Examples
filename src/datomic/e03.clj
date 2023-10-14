(ns datomic.e03)

(require '[datomic.client.api :as d])

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))

(d/delete-database client {:db-name "db03"})
(d/create-database client {:db-name "db03"})

(def conn (d/connect client {:db-name "db03"}))

(d/transact
  conn
  {:tx-data [{:db/ident :red}
             {:db/ident :green}
             {:db/ident :blue}]})

; Not: Daha önceki örneklerde hep önce schema oluşturuyorduk.
;; Burada schema oluşturmadık.
;
;; Neden böyle?
;; Şimdi öncelikle enum dediğimiz şeyler, daha önceki schemadaki gibi birer atribut değil.
;; Yani bir varlık tipinin bir kolonu (atributu) değil.
;; Daha önce Öğrenci varlık tipini (tablosunu) oluştururken, öğrencinin atributlarını tanımlıyorduk schema içinde
;; Neydi bu atributlar? öğrenci_ismi, öğrenci_adresi, ...
;; Fakat :red, :green gibi renkler, herhangi bir kolon ismi veya bir tablonun bir kolonu veya atributu değil.
;
;; Fakat ben renk kümesini, örneğin diğer enum tiplerinden ayırmak isteyebilirim.
;; Mesela giysi tipi diye başka bir kategorik kümem olsun.
;; Giysi_Tipi kümesinde, 3 tane enum değeri olsun: :shirt :dress :hat
;; Şimdi bunları :db/ident ile tanımlayabilirim.
;; Peki ama bu enumlar renk enumlarıyla karışmaz mı bu durumda?
;; Nasıl ayırt edebilirim bunları?
;; namespace ile ayırt edebiliriz

(d/transact
  conn
  {:tx-data [{:db/ident :color/red}
             {:db/ident :color/green}
             {:db/ident :color/blue}]})

(d/transact
  conn
  {:tx-data [{:db/ident :clothing/shirt}
             {:db/ident :clothing/dress}
             {:db/ident :clothing/hat}]})


(def db-schema
  [{:db/ident :product/name
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :product/color
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data db-schema})

(def product-list
  [{:product/name "Kalem"
    :product/color :color/red}
   {:product/name "Kalem"
    :product/color :color/blue}
   {:product/name "Defter"
    :product/color :color/red}
   {:product/name "Defter"
    :product/color :color/green}])

(d/transact conn {:tx-data product-list})

;(def product-list-2
 ; [{:product/name "Kalem"
;    :product/color :color/purple)

;d/transact conn {:tx-data product-list-2})

;bunu diyince hata veriyor
; Hata veriyor. Çünkü ref verdiğim :color/purple objesini bulamıyor.

(type :color/red)

(def order-schema
  [{:db/ident :order/product
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :order/size
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data order-schema})

(def order-list
  [{:order/product 74766790688850
    :order/size 5}
   {:order/product 74766790688852
    :order/size 4}])

(d/transact conn {:tx-data order-list})
(def db (d/db conn))

(d/q
  '[:find ?e
    :where
    [?e :product/name "Kalem"]
    [?e :product/color :color/red]]
  db)

;=> [[79164837199954]]
; ama bu gelen sonuç bir vektörün içindeki vektör olarak geldi
; içindeki datayı çekmek için: ffirst

(def product-id
  (ffirst (d/q
            '[:find ?e
              :where
              [?e :product/name "Kalem"]
              [?e :product/color :color/red]]
            db)))

(identity product-id)

(def order-list
  [{:order/product product-id
    :order/size 6}])

(d/transact conn {:tx-data order-list})

(def db (d/db conn))

(d/q
  '[:find ?order ?size
    :where
    [?e :product/name "Kalem"]
    [?e :product/color :color/red]
    [?order :order/product ?e]
    [?order :order/size ?size]]
  db)
;=> [[74766790688854 6]]

; Konu: Pull API

; Şu ana kadar sonuç içinde döndürmek istediğimiz tüm atributları find içine tek tek yazıyorduk
; Bunun daha pratik bir yolu yok mu?
; Var: Pull API

(d/q
  '[:find (pull ?order [*])
    :where
    [?e :product/name "Kalem"]
    [?e :product/color :color/red]
    [?order :order/product ?e]]
  db)

(d/q
  '[:find (pull ?order [*])
    :in $ ?color
    :where
    [?e :product/name "Kalem"]
    [?e :product/color ?color]
    [?order :order/product ?e]]
  db :color/red)

; birden çok keywork referansı sorgu parametresi olarak vermek
(d/q
  '[:find (pull ?order [*])
    :in $ [?colors ...]
    :where
    [?e :product/name "Kalem"]
    [?e :product/color ?colors]
    [?order :order/product ?e]]
  db [:color/red])
