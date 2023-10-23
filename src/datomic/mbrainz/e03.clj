(ns e03)

(require '[datomic.api :as d])

(def uri "datomic:dev://localhost:4334/mbrainz-1968-1973")

(def conn (d/connect uri))

(def db (d/db conn))

(take 10 (d/q '[:find ?year ?title
                :where
                [?date :release/year ?year]
                [(= ?year 1970)]
                [_ :track/name ?title]]
              db))

;=>
;([1970 "You're Everything"]
; [1970 "Clyde"]
; [1970 "Whisper"]
; [1970 "The Ringmaster"]
; [1970 "Sidewalk Cafe"]
; [1970 "No, I'm Never Gonna Give Ya Up (instrumental)"]
; [1970 "Relax"]
; [1970 "Main Shair To Nahin"]
; [1970 "Il ratto della chitarra"]
; [1970 "Wailing of the Willow"])

(take 10 (d/q '[:find ?year
                :in $ ?date
                :where
                [_ :release/year ?year]
                [(= ?year ?date)]]
              db 1970))


;=> ([1970])
; Burada yılı 1970 e eşit olanların hepsini getir dedik.


