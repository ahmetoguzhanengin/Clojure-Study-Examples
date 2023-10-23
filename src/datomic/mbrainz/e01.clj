(ns e01)

(use '[datomic.api :only [q db] :as d])
; (def uri "datomic:mem://movies")
(def uri "datomic:dev://localhost:4334/mbrainz-1968-1973")
(d/create-database uri)
(def conn (d/connect uri))

(q '[:find ?n
     :where
     [:db.part/db :db.install/attribute ?a]
     [?a :db/ident ?n]]
   (db conn))