(ns datomic.e21)

(require '[datomic.client.api :as d])

; Hata: not-a-data-function

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))

(d/create-database client {:db-name "db21"})

(def conn (d/connect client {:db-name "db21"}))

(def db (d/db conn))

(def schema
  [{:db/ident       :dogum_yeri
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data schema})

(def data {:dogum_yeri "Ankara"})
;=> []

(d/q
  '[:find ?e
    :where
    [?e :dogum_yeri "Ankara"]]
  db)

(def data [{:dogum_yeri "Ankara"}])

(d/transact conn {:tx-data data})

;=> []

(def db (d/db conn))


(d/q
  '[:find ?e
    :where
    [?e :dogum_yeri "Ankara"]]
  db)

;=> [[87960930222154]]


; Problemin sebebi:
; Datomic'de connection objesiyle db (veritabanı) objesi birbirinden farklıdır.
; connection objesi her zaman size en güncel veritabanı objesini döner.
; Ancak veritabanı objesi (db) yaratıldığı andaki veritabanının durumunu yansıtır.
; db objesi ilk yaratıldığında henüz daha `:dogum_yeri` atributu yoktu
; Bu yüzden sorgulama ilk başta hata verdi.
; İkinci sorgulamada db'yi yeni baştan çektik. En güncel veritabanı durumundan çektik.
; Bu yüzden orada hata vermedi.



