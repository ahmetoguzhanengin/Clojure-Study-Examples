(ns datomic.e05)

(require '[datomic.client.api :as d])

(def client (d/client {:server-type :dev-local
                       :storage-dir :mem
                       :system "dev"}))

(d/create-database client {:db-name "Motorycle_Sell_Platform"})

(def conn (d/connect client {:db-name "Motorycle_Sell_Platform"}))

(def db (d/db conn))

(d/transact
  conn
  {:tx-data [{:db/ident :color/white}
             {:db/ident :color/black}
             {:db/ident :color/metalic-grey}
             {:db/ident :color/yellow}
             {:db/ident :color/red}]})

(d/transact
  conn
  {:tx-data [{:db/ident :condition/new}
             {:db/ident :condition/used}
             {:db/ident :condition/broken}]})

(def schema-1
  [{:db/ident :product/company
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/cardinality :db.cardinality/one}
   {:db/ident :product/model
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :product/year
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :product/motohp
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   {:db/ident :product/color
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident       :product/condition
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data schema-1})

(def motorcycles [{:product/company "Yamaha"
                   :product/model "Mt 07"
                   :product/year "2015"
                   :product/motohp "74.80"
                   :product/color :color/yellow
                   :product/condition :condition/used}
                  {:product/company "Kuba"
                   :product/model "Superlight"
                   :product/year "2021"
                   :product/motohp "10.46"
                   :product/color :color/black
                   :product/condition :condition/new}
                  {:product/company "Mondial"
                   :product/model "Rx3i Evo"
                   :product/year "2015"
                   :product/motohp "24"
                   :product/color :color/black
                   :product/condition :condition/new}])

(d/transact conn {:tx-data motorcycles})

@(def db (d/db conn))

(def order-schema
  [{:db/ident :order/product
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :product/condition
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/one}
   {:db/ident :order/size
    :db/valueType :db.type/long
    :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data order-schema})



(defn get-entity-ib-by-moto-company [company-string]
  (ffirst (d/q
            '[:find ?e
              :in $ ?company-string
              :where
              [?e :product/company ?company-string]]db company-string)))

(get-entity-ib-by-moto-company "Kuba")


(defn get-entity-ib-by-moto-company [company-string]
  (ffirst (d/q
            '[:find ?e
              :where
              [?e :product/company "Kuba"]] db)))

(get-entity-ib-by-moto-company "Kuba")



























