(ns datomic.e19)

;Tarih: 20231018

; Title: API

(require '[datomic.client.api :as d])

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))
(d/create-database client {:db-name "db19"})

(def conn (d/connect client {:db-name "db19"}))

(def db (d/db conn))

(def schema
  [{:db/ident       :user/name
    :db/doc         "The unique username of a user."
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}])

(d/transact conn {:tx-data schema})
(def data01
  [{:user/name    "Ahmet"}
   {:user/name    "Metin"}])

(def tx-result
  (d/transact conn {:tx-data data01}))


(class tx-result)
; clojure.lang.PersistentArrayMap

(keys tx-result)
;Clojure programlama dilinde keys fonksiyonu, bir map veri yapısının anahtarlarını döndüren bir işleve karşılık gelir. Bu fonksiyon, bir map'in anahtarlarına erişmek için kullanılır.

;=> (:db-before :db-after :tx-data :tempids)

(:db-before tx-result)

;=>
;#datomic.core.db.Db{:id "b35d2a83-19d3-4a84-8190-c0c37e728baf",
;                    :basisT 6,
;                    :indexBasisT 0,
;                    :index-root-id nil,
;                    :asOfT nil,
;                    :sinceT nil,
;                    :raw nil}


(d/q '[:find ?e
       :where
       [?e :user/name "Ahmet"]]
     (:db-after tx-result))
;=> [[79164837199946]]

(d/q '[:find ?e
       :where
       [?e :user/name "Ahmet"]]
     (:db-after tx-result))

;[]

(d/q '[:find ?e
       :where
       [?e :user/name "Ahmet"]]
     db)
;=> [[79164837199946]]

;`:db-after` `:db-before` ve `d/db`
;':db-after' = güncellemeden sonraki halini  getirir
;':db-before'= güncellemeden önceki halini getirir
;':d/db' = en güncel hakini getirir.

(:tx-data tx-result)

;=>
;[#datom[13194139533319 50 #inst"2023-10-18T07:12:35.873-00:00" 13194139533319 true]
; #datom[79164837199946 73 "Ahmet" 13194139533319 true]
; #datom[79164837199947 73 "Metin" 13194139533319 true]]

;İlk sayı (örneğin, 13194139533319): Bu, veri öğesinin benzersiz bir kimliğini temsil edebilir. Bu kimlik, veri öğesine özgüdür ve bu nesnenin diğer parçalarında referans alınabilir.
;Tarih/Zaman (örneğin, #inst"2023-10-18T07:12:35.873-00:00"): Bu, belirli bir tarihi ve zamanı temsil eder. Bu durumda, 2023-10-18T07:12:35.873-00:00 tarihi ve saati ifade ediyor.
;Boolean değer (örneğin, true): Bu, bir mantıksal değeri temsil eder ve muhtemelen bir durumu belirtir (örneğin, "aktif" veya "pasif").


(d/q
  '[:find ?i
    :where
    [50 ?a]
    [?a :db/ident ?i]]
  db)

;=> [[:db/doc] [:db/valueType] [:db/ident] [:db/cardinality]]

;[50 ?a]: Bu satırda 50 değeri, ?a değişkeni ile ilişkilendirilir. Bu, ?a değişkeninin 50 ile eşleştiği anlamına gelir.

;Bu sorgu, db adlı bir veritabanında çalıştırılacaktır. Sonuç olarak, bu sorgu ?i değişkenine 50 ile ilişkilendirilmiş olan Datomic kimliğini (ident) döndürecektir.
;
;Sonuç olarak, sorgunun çalıştırılması sonucunda ?i değişkenine, 50 ile ilişkilendirilmiş olan Datomic kimliği (ident) atanacaktır. Bu kimlik, Datomic veritabanındaki bir öğeyi veya kaynağı temsil edebilir ve daha fazla sorgu veya işlemde kullanılabilir.





















