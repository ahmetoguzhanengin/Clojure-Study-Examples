(ns datomic.e18)

; Tarih : 20231018
; Title: Temp idlerle referans verme

(require '[datomic.client.api :as d])

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))

(d/create-database client {:db-name "db18"})

(def conn (d/connect client {:db-name "db18"}))

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

(d/transact conn {:tx-data data01})

(def db (d/db conn))

(d/q '[:find ?e ?name
       :where [?e :user/name ?name]]
     db)

(def schema02
  [{:db/ident       :user/friends
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many}

   {:db/ident       :user/admin?
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data schema02})

(d/transact conn {:tx-data [{:db/id        "user1"
                             :user/name    "Evren"
                             :user/friends #{"user2"}}

                            {:db/id        "user2"
                             :user/name    "Hilal"
                             :user/admin?  true
                             :user/friends #{[:user/name "Metin"]}}]})

(def db (d/db conn))

(d/q '[:find ?e
       :where [?e :user/friends _]]
     db)

;=> [[74766790688844] [74766790688845]]

