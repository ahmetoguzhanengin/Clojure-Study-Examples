(ns datomic.e20)

(require '[datomic.client.api :as d])

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))

(d/create-database client {:db-name "db20"})

(def conn (d/connect client {:db-name "db20"}))

;; Memory storage

;a01
(d/transact conn [{:db/ident       :user/name
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity}])

;=>
;{:db-before #datomic.core.db.Db{:id "525dabfe-c577-43b6-a8cf-ebf902fc8906",
;                                :basisT 5,
;                                :indexBasisT 0,
;                                :index-root-id nil,
;                                :asOfT nil,
;                                :sinceT nil,
;                                :raw nil},
; :db-after #datomic.core.db.Db{:id "525dabfe-c577-43b6-a8cf-ebf902fc8906",
;                               :basisT 6,
;                               :indexBasisT 0,
;                               :index-root-id nil,
;                               :asOfT nil,
;                               :sinceT nil,
;                               :raw nil},
; :tx-data [#datom[13194139533318 50 #inst"2023-10-18T07:56:37.953-00:00" 13194139533318 true]],
; :tempids {}}

(def tx-result
  (d/transact conn [{:user/name    "Oğuzhan"}
                    {:user/name    "Evren"}]))

(d/q '[:find ?e ?name
       :where [?e :user/name ?name]]
     (d/db conn))

;Execution error (ClassNotFoundException) at datomic.core.datalog/throw-query-ex! (datalog.clj:46).
;com.google.common.util.concurrent.UncheckedExecutionException

; a02:
(def schema
  [{:db/ident       :user/name2
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}])

(d/transact conn {:tx-data schema})

(def data [{:user/name2 "un01"}])

(d/transact conn {:tx-data data})

(d/q
  '[:find ?e
    :where
    [?e :user/name2 "un01"]]
  (d/db conn))

;=> [[96757023244362]]


; Problemin sebebi:
; a01 ve a02 arasındaki tek fark, schema ilkinde transact fonksiyonuna :tx-data olmadan paslanmış
; ikincisinde :tx-data ile paslanmış.

; Dikkat:
; a01:
;(d/transact conn [{:db/ident       :user/name}])
; a02:
;(d/transact conn {:tx-data schema})









