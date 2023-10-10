(ns datomic.e01)

(require '[datomic.client.api :as d])
(def client (d/client {:server-type :datomic-local
                       :storage-dir :mem
                       :system "dev"}))

(d/create-database client {:db-name "db01"})

(def conn (d/connect client {:db-name "db01"}))

(def schema-1
  [{:db/ident :ogrenci_id
    :db/valueType :db.type/long
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :isim
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :sehir
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}])

;;Yeni bir kolon eklemek istersek
;(def schema-1
;;  [{:db/ident :ogrenci_id2
;;    :db/valueType :db.type/long
;;    :db/unique :db.unique/identity
;;    :db/cardinality :db.cardinality/one}])

; Mesela: 3. kolonun ismi `sehir` değil `city` olsun. Bunu programatik olarak değiştirelim.
(assoc-in schema-1 [2 :db/ident] "city")

;Şu ana bu kadar bu schema düz clojure verisi (plain clojure data).
; POJO: plain old java object.
; prensip: düz veri objeleriyle çalışmak her zaman iyidir.

; Neden genel olarak düz veri objeleriyle çalışmak tercih edilir programlamada?
; rfr: Rich Hickey, data passive vs. terimleriyle alakalı

; Bu düz veriyi henüz veritabanına koymadık.
; Bunu veritabanına koymak için `transact` fonksiyonunu kullanırız.

(d/transact conn {:tx-data schema-1})

; bu işlemle tablomuzun kolonlarını tanımlamış olduk sadece

@(def data
   [{:ogrenci_id 101
     :isim "Ahmet Oğuzhan"
     :sehir "Bolu"}
    {:ogrenci_id 102
     :isim "Metin Burak"
     :sehir "Bartın"}])

(d/transact conn {:tx-data data})

; Dikkat: data: list of hashmap = list of entity

;Şimdi EAV tablomuzu veritabanımıza kaydetmiş olduk.
;| E     | A       | V             |
;|-----  |-------  |-------------  |
;| 101   | isim    | Ahmet Oğuzhan |
;| 101   | şehir   | Bolu          |
;| 102   | isim    | Metin Burak   |
;| 102   | şehir   | Bartın        |


;Sorgulama yapmadan önce veritabanının en güncel halini çekelim

(def db (d/db conn))

(d/q
  '[:find ?e
    :where
    [?e :sehir "Bolu"]]
  db)

(d/q
  '[:find ?i
    :where
    [?e :sehir "Bolu"]
    [?e :isim ?i]]
  db)

;q: entity'nin id'si neden 101 gelmedi de 101... geldi?
; onun için `ogrenci_id` atributunu çekmemiz lazım.

(d/q
  '[:find ?oid
    :where
    [?e :sehir "Bolu"]
    [?e :ogrenci_id ?oid]]
  db)

;ismi metin burak olanın şehrini bulmam

(d/q
  '[:find ?i
    :where
    [?e :isim "Metin Burak"]
    [?e :sehir  ?i]]
  db)


(d/q
  '[:find ?oid
    :where
    [?e :sehir "Bolu"]
    [?e :ogrenci_id ?oid]]
  db)

; sehir değeri Bolu olan entitylerin tüm atributlarını görmek istiyorum

(d/q
  '[:find ?a
    :where
    [?e :sehir "Bolu"]
    [?e ?a _]]
  db)

;=> [[73] [74] [75]]
; bu şekilde yapınca 3 tane atributumuz vardı ya, ogrenci_id, isim, sehir.
; bunların entity_id'lerini almış olduk
; peki bu atributların id'lerini değil de isimlerini alabilir miyiz?

(d/q
  '[:find ?aname
    :where
    [?e :sehir "Bolu"]
    [?e ?a _]
    [?a :db/ident ?aname]]
  db)


; Dikkat: 4 tane kv ikilimiz var. Yukarıda da 4 tane datom var
; Demek ki, her bir kv ikilisi için, bir datom tanımlanmış.

