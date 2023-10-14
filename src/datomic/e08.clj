(ns datomic.e08)

; Konu: Find Specifications

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])
(def db (d/db conn))


; Şu ana kadar Parametreleri bağlarken, farklı türlerde objeleri parametre olarak gönderebileceğimizi gördük.
; Yani illa primitif olması gerekmiyordu.
; Benzer şekilde sorgu sonuçlarının da illa primitif düz olması gerekmiyor.
; Daha sofistike yapılarda sorgu sonuçlarını şekillendirebiliriz.

; Bunun için Find Specifications kullanıyoruz.
;| Find Spec     | Returns       | Java Type Returned  |
;|---------------|---------------|---------------------|
;| :find ?a ?b   | relation      | Collection of Lists |
;| :find [?a …]  | collection    | Collection          |
;| :find [?a ?b] | single tuple  | List                |
;| :find ?a .    | single scalar | Scalar Value        |


(d/q
  '[:find ?eid ?name
    :where
    [?eid :product/name ?name]]
  db)
;=> [[92358976733264 "Kalem"] [92358976733263 "Kalem"] [92358976733266 "Defter"] [92358976733265 "Defter"]]
; Bir vektörün içinde vektörler dönüyor
; İç vektör her zaman aynı sayıda öğeden oluşuyor
; Dolayısıyla bu yapıyı biz tablo şeklinde gösterebiliriz
; 92364 "Kalem"
; 92363 "Kalem"
; 92366 "Defter"
; 92365 "Defter"
; Dolayısıyla aslında veriyi düz tablo formatında dönmüş oluyor.

; 2. Tip: Collection
(flatten (d/q
           '[:find ?eid
             :where
             [?eid :product/name _]]
           db))
;=> (92358976733265 92358976733266 92358976733263 92358976733264)

; 3. Tip: Single Tuple
(flatten (d/q
           '[:find ?eid ?color
             :where
             [?eid :product/id 1]
             [?eid :product/color ?color]]
           db))
;=> (92358976733266 74766790688844)

