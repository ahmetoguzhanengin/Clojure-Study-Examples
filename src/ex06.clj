(ns ex06)

(def system { :person [{:person/id 1 :name "Ahmet" :surname "Engin" :joindate "01.10.2023" :experience :experience/starter :worktime :worktime/part-time :work-type :work-type/internship :managers {:manager/id 1}}
                       {:person/id 2 :name "Hilal" :surname "Hatunoğlu" :joindate "02.10.2023" :experience :experience/mid :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 1}}
                       {:person/id 3 :name "Metin" :surname "Bağdatlı" :joindate "03.10.2023" :experience :experience/senior :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 1}}
                       {:person/id 4 :name "Can" :surname "Duyar" :joindate "04.10.2023" :experience :experience/mid :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 2}}
                       {:person/id 5 :name "Evren" :surname "Çetinkaya" :joindate "05.10.2023" :experience :experience/senior :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 2}}
                       ]

             :relation/experience {:experience/starter {:experience-time-period "0-1"}
                                   :experience/mid {:experience-time-period "1-3"}
                                   :experience/senior {:experience-time-period "3-6"}
                                   :experience/lead {:experience-time-period "6+"}}

             :relations/worktime  #{:worktime/full-time :worktime/part-time}
             :relations/work-type #{:work-type/internship :work-type/tenure}

             :employees/team [:frontend [{:person/id 3} {:person/id 2}]

                              :backend [{:person/id 5}]
                              :fullstack [{:person/id 1}]
                              :data[{:person/id 4}]
                              ]
             :employees/managers [{:manager/id 1 :manager/name "Mert" :manager/surname "Nuhoğlu" :manager/person [{:person/id 1} {:person/id 2} {:person/id 3}]}
                                  {:manager/id 2 :manager/name "Barış" :manager/surname "Şenyeli" :manager/person [{:person/id 4} {:person/id 5}]}]}
  )


(->> system
     (:person)
     (filter #(= (:person/id %) 1))
     (first)
     (:experience)

     (get-in system [:relation/experience (->> system
                                               (:person)
                                               (filter #(= (:person/id %) 1))
                                               (first)
                                               (:experience))])
     (:experience-time-period)
     )


;;(get-in m ks default)
;;m: Erişim sağlamak istediğimiz harita (map).
;;ks: Bir anahtar dizisi (vector), haritanın içindeki değere ulaşmak için kullanılır. Örneğin, [:a :b :c] şeklinde bir anahtar dizisi, m haritasında :a haritasının içindeki :b haritasının içindeki :c anahtarının değerine erişmek için kullanılır.
;;default: Eğer belirttiğimiz anahtarlar dizisi ile bir değer bulunamazsa, döndürülecek varsayılan değer.


(get-in system [:relation/experience
                (:experience
                  (first (filter #(= 1 (:person/id %)) (:person system))))
                :experience-time-period])
; q01: verili bir elemanın yöneticisinin ismini alalım
; mesela lodosun yöneticisi kimdir?
(->> system
     (:person))
;=>

(->> system
     (:person)
     (filter #(-> (:name %1)
                  (= "Ahmet")))
     (first))

;;({:person/id 1,
;  :name "Ahmet",
;  :surname "Engin",
;  :joindate "01.10.2023",
;  :experience :experience/starter,
;  :worktime :worktime/part-time,
;  :work-type :work-type/internship,
;  :managers #:manager{:id 1}})

(def p1
  (->> system
       (:person)
       (filter #(-> (:name %1)
                    (= "Ahmet")))
       (first)))
(identity p1)

; şimdi p1 objesinin (yani başka bir deyişle Ahmet kişisinin) yöneticilerini bulalım

(->> p1
     (:managers))

(->> p1
     (:managers)
     (:manager/id))

;=> 1

; buna da bir isim verelim
(def searched_manager (->> p1
                           (:managers)
                           (:manager/id)))

; şimdi :employees/managers tablosundaki ilgili satırı alalım (1 değerli satırı)
; bunun için önce bu tabloya bir isimle erişebilir olalım

(def ms (:employees/managers system))

(print ms)

/Users/ahmetoguzhanengin/Library/CloudStorage/GoogleDrive-ahmetoguzhanengin@gmail.com/.shortcut-targets-by-id/1MVLjOzfNXnybX8FKc3bDpLYXD4psqU9_/grsm/opal/ekip/ahmetoguzhanengingm