(ns ex11
  (:require [portal.api :as p]
           [clojure.java.io :refer [reader] :as io]
           [clojure.edn :as edn]
           [clojure.data :refer [diff]]
           [clojure.pprint :refer [pprint]]))

;portalı açmak için
(def portal (p/open))

(p/tap)
(tap> :hello)
(tap> {:a 1 :b 2})

(def database { :person [{:person/id 1 :name "Ahmet" :surname "Engin" :joindate "01.10.2023" :experience :experience/starter :worktime :worktime/part-time :work-type :work-type/internship :managers {:manager/id 1}}
                         {:person/id 2 :name "Hilal" :surname "Hatunoğlu" :joindate "02.10.2023" :experience :experience/mid :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 1}}
                         {:person/id 3 :name "Metin" :surname "Bağdatlı" :joindate "03.10.2023" :experience :experience/senior :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 1}}
                         {:person/id 4 :name "Can" :surname "Duyar" :joindate "04.10.2023" :experience :experience/mid :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 2}}
                         {:person/id 5 :name "Evren" :surname "Çetinkaya" :joindate "05.10.2023" :experience :experience/senior :worktime :worktime/full-time :work-type :work-type/tenure :managers {:manager/id 2}}]


               :relation/experience {:experience/starter {:experience-time-period "0-1"}
                                     :experience/mid {:experience-time-period "1-3"}
                                     :experience/senior {:experience-time-period "3-6"}
                                     :experience/lead {:experience-time-period "6+"}}

               :relations/worktime  #{:worktime/full-time :worktime/part-time}
               :relations/work-type #{:work-type/internship :work-type/tenure}

               :employees/team [{:frontend [[:person/id 1] [:person/id 3]]

                                 :backend [[:person/id 5]]
                                 :fullstack [[:person/id 2]]
                                 :data[[:person/id 4]]}]

               :employees/managers [{:manager/id 1 :manager/name "Mert" :manager/surname "Nuhoğlu" :manager/person [:person/id 1]}
                                    {:manager/id 2 :manager/name "Barış" :manager/surname "Şenyeli" :manager/person [:person/id 2]}]})


(tap> database)

(tap> :database)