(ns datomic.e06)

; Konu: Parametrik Sorgular (Parameterised Queries)

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn db] :as e03])

(d/q '[:find ?e
       :where
       [?e :product/name "Kalem"]] db)
;=> [[101155069755474] [101155069755475]]


(d/q
  '[:find ?e
    :in $ ?product-name
    :where
    [?e :product/name ?product-name]]
  db "Kalem")

;=> [[101155069755474] [101155069755475]]

; Burada :in cümleciğinde (clause) iki tane argüman var.
; İlk argüman: `$`
; Bu ilk argüman `db` argümanıyla eşleşir.
; İkinci argüman: `?product-name`
; Bu ise "Kalem" argümanıyla eşleşir.
; data pattern (yani sorgu cümleciklerimiz) hep EAV formatında diyorduk.
; aslında daha başka veriler de var. fakat onları ihmal edebiliyoruz.
; normalde data patternın formatı şu şekilde:
; [<database> <entity-id> <attribute> <value> <transaction-id>]
; aslında data pattern içindeki ilk argüman ilgili database objesine denk gelir
; ama burada dikkat edersek: 5 tane argüman içeriyor data pattern
; eğer sen 3 tane argüman gönderirsen: EAV formatını varsayar
; eğer 2 arg gönderirsen: EA varsayar
; eğer 4: DB EAV
; eğer 5 gönderirsen: DB EAV Tx

(d/q
  '[:find ?e
    :in $ ?product-name
    :where
    [$ ?e :product/name ?product-name]]
  db "Kalem")
;=> [[101155069755474] [101155069755475]]

(defn find-product-by-name [product-name]
  (d/q
    '[:find ?e
      :in $ ?product-name
      :where
      [?e :product/name ?product-name]]
    db product-name))

(find-product-by-name "Kalem")
;=> [[101155069755474] [101155069755475]]

(defn find-product-by-name-wrong [product-name]
  (d/q
    '[:find ?e
      :where
      [?e :product/name product-name]]
    db))

(find-product-by-name-wrong "Kalem")

; Hiçbir şey dönmüyor.
; Neden?
; Çünkü d/q'nun sorgu cümlesi olan argüman aslında escapelenmiş bir formdur.
; Escapelenmiş olan formlar da eval edilmez. Daha sonra başka bir şekilde çalıştırılır.
