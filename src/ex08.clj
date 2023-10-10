(ns ex08)
(def database { :person [{:person/id 1 :name "Ahmet" :surname "Engin" :joindate "01.10.2023" :experience :experience/starter :worktime :worktime/part-time :work-type :work-type/internship :managers {:manager/id 1}}
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

             :employees/team [{:frontend [[:person/id 1] [:person/id 3]]

                               :backend [[:person/id 5]]
                               :fullstack [[:person/id 2]]
                               :data[[:person/id 4]]}
                              ]
             :employees/managers [{:manager/id 1 :manager/name "Mert" :manager/surname "Nuhoğlu" :manager/person [:person/id 1 ]}
                                  {:manager/id 2 :manager/name "Barış" :manager/surname "Şenyeli" :manager/person [:person/id 2]}]}
  )

(defn get-person-by-id "get person by :person/id value" [id]
  (->> database
       (:person)
       (filter #(= (:person/id %1) id))
    )
  )
(comment
  (get-person-by-id 1)
  ;({:person/id 1,
  ;  :name "Ahmet",
  ;  :surname "Engin",
  ;  :joindate "01.10.2023",
  ;  :experience :experience/starter,
  ;  :worktime :worktime/part-time,
  ;  :work-type :work-type/internship,
  ;  :managers #:manager{:id 1}})
  )

(defn get-manager-person-id-by-manager-id "get a manager name my given manager id" [id]
  (->> database
       (:employees/managers)
       (filter #(= (:manager/id %) id))
       (first)
       (:manager/person)
       (second)
       ))

(comment
  (get-manager-person-id-by-manager-id 1))
;;=> #:person{:id 2}

(defn get-person-name-by-id "gett a person name by given id" [id]
  (->> database
       (:person)
       (filter #(= (:person/id %1) id))
       (first)
       (:name)
       )
  )


(comment
  (get-person-name-by-id 1)
  )
;=> "Ahmet"

(defn get-manager-name-by-id "get manager name by id" [id]
  (->> database
      (:employees/managers)
      (filter #(= (:manager/id %1) id))
      (first)
      (:manager/name)
      )
     )
()
(defn get-person-manager-name-by-person-id "get a person's manager name by given person id" [id]
  (-> id
      (get-person-by-id)
      (first)
      (:managers)
      (:manager/id)
      (get-manager-person-id-by-manager-id)
      (get-manager-name-by-id)
      )
  )

(comment
  (get-person-manager-name-by-person-id 1))
;;=> "Mert"

(defn get-frontend-developer-team-members-ids "get frontend developer team members references" []
  (->> database
       (:employees/team)
       (first)
       (:frontend)
       )
  )
(comment
  (get-frontend-developer-team-members-ids)
  )

(defn get-person-by-keyword-value-pair "get person by giving keyword and value pair" [kw value]
  (->> database
       (:person)
       (filter #(= (kw %1) value))
       )
  )

(comment
  (get-person-by-keyword-value-pair :name "Ahmet")
  )
;=>
;({:person/id 1,
;  :name "Ahmet",
;  :surname "Engin",
;  :joindate "01.10.2023",
;  :experience :experience/starter,
;  :worktime :worktime/part-time,
;  :work-type :work-type/internship,
;  :managers #:manager{:id 1}})

(defn get-person-by-name-by-surname "get person surname by given name" [name]
  (->> database
       (:person)
       (filter #(= (:name %1) name))
       (first)
       (:surname)))

(comment
  (get-person-by-name-by-surname  "Ahmet")
  )

(defn get-person-id-by-given-name "get person id by given name" [name]
  (->> database
       (:person)
       (filter #(= (:name %1) name))
       (first)
       (:person/id)
       )
  )

(comment
  (get-person-id-by-given-name "Ahmet")
  )
;;first dersek :person/id yi alıyor eğer second dersek id değerlerini alıyor
(comment
(map second (get-frontend-developer-team-members-ids))
  )


;;frontend developerların isimlerini almak için
(comment
  (->> (map second (get-frontend-developer-team-members-ids))
       (map get-person-name-by-id)
       )
  )

;;müdürlerini bulmak için
(comment
  (->> (map second (get-frontend-developer-team-members-ids))
       (map get-person-manager-name-by-person-id)
       )
  )

(defn employee-names [] (->>
                          (map second (get-frontend-developer-team-members-ids))
                          (map get-person-name-by-id)
                          ))
(comment
  (employee-names)
  )

(defn manager-names [] (->>
                         (map second (get-frontend-developer-team-members-ids))
                         (map get-person-manager-name-by-person-id)
                         ))

(manager-names)

(defn get-employee-name-and-their-managers [] (->>
                                                (map vector (employee-names) (manager-names))
                                                (map #(str "employee/manager: " %))
                                                (print)
                                                ))

(comment
  (get-employee-name-and-their-managers)
  )

;Clojure'da zipmap işlevi, iki vektörün elemanlarını eşleyerek bir map oluşturan bir işlevdir. İlk vektör, anahtarlar olarak kullanılırken, ikinci vektör değerler olarak kullanılır. Bu işlev, sıklıkla iki vektörün birbirine karşılık gelen elemanlarını birleştirmek için kullanılır.

;(defn square [x] (* x x))
;
;(def keys [1 2 3 4])
;(def values (map square keys))
;
;(def square-map (zipmap keys values))
;=> {1 1, 2 4, 3 9, 4 16}

(defn get-employee-name-and-their-managers-map [] (->>
                                                    (zipmap (employee-names) (manager-names))
                                                    (map #(str "employee/manafer: " %))
                                                    ))

(comment
  (get-employee-name-and-their-managers-map)
  )
;=> ("employee/manafer: [\"Ahmet\" \"Mert\"]" "employee/manafer: [\"Metin\" \"Mert\"]")
(zipmap (employee-names) (manager-names))
;=> {"Ahmet" "Mert", "Metin" "Mert"}

;into fonksiyonu, bir koleksiyonu başka bir koleksiyona dönüştürmek için kullanılır. Bu, bir koleksiyondan başka bir türdeki koleksiyona veri kopyalamak için kullanışlıdır.
(into [] (zipmap (employee-names) (manager-names)))
;=> [["Ahmet" "Mert"] ["Metin" "Mert"]]

;eğer [] yerine {} kullansaydım
;=> {"Ahmet" "Mert", "Metin" "Mert"}

; hedef:
; [{:employee "lodos", :manager "mert"}
;  {:employee "alp", :manager "shelby"}
;  ...]

; bir veri modeli veya veri yapısının biçimini (şekil, shape) değiştirmek işlemine, transformasyon denir.
; transformasyon: biçimini çevirmek anlamına gelir latince/yunancada
; prensip: transformasyon yaparken, önce hedef biçimi koment olarak yaz
; bunu direk yazılı olarak görmek zihninin daha iyi odaklanmasını sağlar
; başına da girdi biçimini yaz
; şablon:
; - girdi biçim
; - hedef biçim

(comment
  (->>
    (zipmap (employee-names) (manager-names))
    )
  )
;girdi:
;=> {"Ahmet" "Mert", "Metin" "Mert"}

;çıktı olarak istediğim şey

;["employee: Ahmet manager: Mert"
; "employee: Metin manager: Mert"
;..]

(->>
  (zipmap (employee-names) (manager-names))
  (map #(str "employee: " (first %) " manager: " (second %)))
  )
;=> ("employee: Ahmet manager: Mert"
;    "employee: Metin manager: Mert")

;Clojure'daki (as-> ...) ifadesi, bir değeri bir dizi işleme tabi tutarak ve bu işlemler arasında geçiş yaparak sonuç elde etmek için kullanılır. Bu yapı, işlemlerin arasındaki geçiş değişkeni olarak belirtilen bir sembolü kullanır.

(as-> 0 n
      (inc n)  ; n is 0 here passed from first parameter to as->
      (inc n)) ; n is 1 here passed from result of previous inc expression

(comment
  (def x 0)
  (-> x
      (inc)
      (inc)))

;;
; a isminde bir atom (obje) oluşturuyorum
(def a (atom 1))
;; => #'user/a

; burada da a objesinin içeriğini çekiyorum
(deref a)
;; => 1

(def b 1)
; normal bir objenin içeriğine erişmek için bir şey yapmama gerek yok
(print b)
;1=> nil
(print (deref a))