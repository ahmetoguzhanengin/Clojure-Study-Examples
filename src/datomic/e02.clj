(ns datomic.e02)

(require '[datomic.client.api :as d])

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))

(d/create-database client {:db-name "db02"})

(def conn (d/connect client {:db-name "db02"}))


(def movie-schema
  [{:db/ident :movie/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the movie"}])

;Fark: Daha önce her bir atributa doğrudan isim vermiştik. Burada namespace ile isim veriyoruz
; :movie/title
; Terim: uzun namespace ile tanımlanmış isimlere, qualified name denir.

; :db/doc Bu atributun ne anlama geldiğine dair bir dokümantasyon oluşturuyor. Bir nevi comment gibi.

(def movie-schema
  [{:db/ident :movie/title
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The title of the movie"}
   {:db/ident :movie/genre
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc "The genre of the movie"}
   {:db/ident :movie/release-year
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc "The year the movie was released in theaters"}])


;trasact yapacağız
(d/transact conn {:tx-data movie-schema})


@(def first-movies [{:movie/title "World War Z"
                     :movie/genre "action/horror"
                     :movie/release-year 2013}
                    {:movie/title "Predator"
                     :movie/genre "action/adventure"
                     :movie/release-year 1987}
                    {:movie/title "Rocky"
                     :movie/genre "sport"
                     :movie/release-year 1976}])

(d/transact conn {:tx-data first-movies})


;Transaction ne demek?
; transaction: işlem
; Veritabanında yaptığımız her bir ekleme, silme, düzeltme, birer işlemdir.
; Bizim `d/transact` fonksiyonunu her çağırışımız bir transaction kaydı oluşturur.
; Neden böyle bir transaction kaydını veritabanına yazmak isteyebiliriz?
; Daha sonra ihtiyacımız olacaktır çünkü.
; Mesela ben bu verileri ne zaman kaydettim?
; Benim bu verileri kaydetme tarihim, yukarıdaki business datasından farklı bir veri türü.
; Bu bir meta veri (aşkın veri).
; Meta: beyond, aşkın, ötesi.
; Meta: kelimesini genellikle bir şeyin özünü tarif etmek için kullanırız.
; Business data (iş verisi): Bu esas iş alanına (business domain) ait olan verileri ifade eder.
; Biz enformasyon sistemi kurarken, aslında hem iş verisini, hem de meta verileri saklamamız gerekiyor.
; İş verisi, esas kullanıcının işine yarayan verilerdir.
; Meta verilerse, güvenlik açısından, kontrol açısından veya başka idari amaçlarla ihtiyaç olabilecek verilerdir.

; İş verilerinin datomlarına baktığımızda, her bir entityye ait olan datomların aynı entity id'ye sahip olduğunu görürüz.

;üç datom hepsi aynı entity_id'ye sahip: 101155069755468
; Neden böyle?
; Çünkü biz bu veriyi kaydederken hepsini aynı mapin içinde göndermiştik:


(def db (d/db conn))

(def all-movies-q '[:find ?e
                    :where [?e :movie/title]])

(d/q all-movies-q db

;=> [[96757023244364] [96757023244365] [96757023244366]]
; Sorgu cümleciklerine data pattern deniyor
;; [?e :movie/title]
;; data pattern: Entity Attribute Value formatında yazılıyor
;; fakat burada iki tane öğe var.
;; bu durumda bu EA'ya karşılık gelir.
; dolayısıyla bu sorgu şu anlama geliyor:

; Bana :movie/title atributu olan tüm entityleri bul. Bunları ?e değişkenine koy

  (def all-titles-q '[:find ?movie-title
                      :where [ _ :movie/title ?movie-title]]))

(d/q all-titles-q db)
;=> [["Rocky"] ["World War Z"] ["Predator"]]
; Buradaki sorgu cümleciğimiz: [_ :movie/title ?movie-title]
; Entity yerine `_` kullanmış. underscore, bunu ihmal et anlamına gelir. Buraya ne geldiği önemli değil.
; Bunu bellekte saklama.
; Buradaki sorguyu şöyle okumalı:
; Bana içinde :movie/title atributu olan tüm entityleri bul.
; Bu entityleri saklama. Ama entitylerin :movie/title atribut değerlerini ?movie-title değişkeni içine koy.

;bizim amacımız mantıksal çıkarımlar yapmak

(def titles-from-1987 '[:find ?title
                        :where
                        [?e :movie/title ?title] [?e :movie/release-year 1987]])

(d/q titles-from-1987 db)
;=> [["Predator"]]

; q: Buradaki ?e değişkenini herhangi bir yerde kullanmadık. find yanında kullansak mantıklı olacak
; ?e değişkenini find içinde kullanabiliriz
(def titles-from-1987b '[:find ?title ?e
                         :where
                         [?e :movie/title ?title]
                         [?e :movie/release-year 1987]])

(d/q titles-from-1987b db)

;=> [["Predator" 96757023244365]]

(def all-data-from-1987 '[:find ?title ?year ?genre
                          :where
                          [?e :movie/title ?title]
                          [?e :movie/release-year ?year]
                          [?e :movie/genre ?genre]
                          [?e :movie/release-year 1987]])

(d/q all-data-from-1987 db)

;=> [["Predator" 1987 "action/adventure"]]


(d/q '[:find ?e
       :where [?e :movie/title "Rocky"]]
     db)

;=> [[96757023244366]]

(def predator-id
  (ffirst (d/q '[:find ?e
                 :where [?e :movie/title "Predator"]]
               db)))

;=> [[96757023244366]]

; ffirst = (first (first))

(first [[96757023244366]])
;=> [96757023244366]
(ffirst [[96757023244366]])
;=> 96757023244366

; Şimdi bu entity_id'ye ait olan filmin genre'sını değiştirelim

(d/transact conn {:tx-data [{:db/id predator-id :movie/genre "futuristic"}]})

(d/q all-data-from-1987 db)

;BUNU YAPMAZSAN GÜNCEL HALİNİ ÇEKMEZ VE ESKİ HALİNİ GÖRÜRSÜN
(def db (d/db conn))

(d/q all-data-from-1987 db)

(identity db)

;;database in eski versiyonuna ulaşmak için
(def old-db (d/as-of db 7))

(identity old-db)

(d/q all-data-from-1987 old-db)


(def hdb (d/history db))


(d/q '[:find ?genre
       :where [?e :movie/title "Predator"]
       [?e :movie/genre ?genre]]
     hdb)

;=> [["futuristic"] ["action/adventure"]]
; Türkçesi:
; Tüm geçmiş veritabanlarında (hdb) bir sorgu yap
; :movie/title değeri "Commando" olan tüm entityleri bul
; Bunların :movie/genre değerlerini ?genre değişkeni içine koy ve göster.

(identity hdb)

(type hdb)
;=> datomic.core.db.Db

; Datom: EAVT cümlesinin toplamı oluyor.
; Fact: EAV oluyor.