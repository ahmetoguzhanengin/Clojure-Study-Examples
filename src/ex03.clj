(ns ex03)


(def mapExp {
             :players [{:adi "Ahmet" :soyadi "Engin"}
                       {:adi "Metin" :soyadi "Bağdatlı"}
                       {:adi "Evren" :soyadi "Çetinkaya"}
                       {:adi "Hilal" :soyadi "Hatunoğlu"}]
             :servers ["bind" "haven" "acsent"]
             :levels [{:adi "Ahmet" :level 2}
                      {:adi "Metin" :level 5}
                      {:adi "Evren" :level 4}
                      {:adi "Hilal" :level 1}]
             } )

(def mapExp {
             :players [{:adi "Ahmet" :soyadi "Engin" :level 2 }
                       {:adi "Metin" :soyadi "Bağdatlı" :level 5}
                       {:adi "Evren" :soyadi "Çetinkaya" :level 4}
                       {:adi "Hilal" :soyadi "Hatunoğlu" :level 1}]
             :servers [{:id 1 :name "Bind" :difficulty :hard}{:id 2 :name "Haven" :difficulty :hard}{:id 3 :name "Accent" :difficulty :hard}]
             } )

(def mapExp {
             :players [{:adi "Ahmet" :soyadi "Engin" :level 2 :player/servers [2 3]}
                       {:adi "Metin" :soyadi "Bağdatlı" :level 5 :player/servers [1 3]}
                       {:adi "Evren" :soyadi "Çetinkaya" :level 4 :player/servers [1]}
                       {:adi "Hilal" :soyadi "Hatunoğlu" :level 1 :player/servers [2]}]
             :servers [{:id 1 :name "Bind" :difficulty :hard}{:id 2 :name "Haven" :difficulty :hard}{:id 3 :name "Accent" :difficulty :hard}]
             } )


;;id si 1 olanın server adını çekme
(->> mapExp
     (:servers)
     (filter #(= (:id %1) 1) )
     (first)
     (:name)
     )
;;=> "Bind"

;;player listesinden Ahmet adli kişiyi çekme

(->> mapExp
     (:players)
     (filter #(= (:adi %1) "Ahmet"))
     (first)
     )
;;=> {:adi "Ahmet", :soyadi "Engin", :level 2, :player/servers [2 3]}

;;=> soyadı engin olan oyuncunun bulunduğu serverların adını yazdırma işlemi
(->> mapExp
     (:players)
     (filter #(= (:soyadi %1) "Hatunoğlu"))
     (first)
     (:player/servers)
     (map #(->> mapExp
                (:servers)
                (filter (fn [m] (= (:id m) %1)))
                (first)
                (:name)
                ))
     )
;;=> ("Haven" "Accent")
;;****************************************************************

;;**************************İkinci Örnek**************************

;;****************************************************************

(def mapExp2 {:users [{:name "Ahmet" :surname "Engin" :department [1] :experience [2]}
                      {:name "Metin Burak" :surname "Bağdatlı" :department [2] :experience [1]}
                      {:name "Evren" :surname "Çetinkaya" :department [3] :experience [3]}
                      ]
              :departments [{:id 1 :department-name "Software"}
                            {:id 2 :department-name "Analysis"}
                            {:id 3 ::department-name "Data"}]

              :experiences [{:id 1 :level "junior"}
                           {:id 2 :level "mid"}
                           {:id 3 :level "senior"}
                           ]

              }
  )

;;kullanıcı adı ahmet olanı nasıl çekerim
(->> mapExp2
     (:users)
     (filter #(= (:name %1) "Ahmet") )
     )
;;kullanıcı adı ahmet olanın departman id sini nasıl çekerim.

(->> mapExp2
     (:users)
     (filter #(= (:name %1) "Ahmet") )
     (first)
     (:department)
     )
;;kullanıcı adı ahmet olanın departman id sine göre departman adına nasıl ulaşırım.
(->> mapExp2
     (:users)
     (filter #(= (:name %1) "Ahmet") )
     (first)
     (:department)
     (map #(->> mapExp2
                (:departments)
                (filter (fn [m] (= (:id m) %1)))
                (first)
                (:department-name)
                )
          )
     )
;;Kullanıcı adı ahmet olanın experience id sine göre experince seviyesine nasıl erişirim.
(->> mapExp2
     (:users)
     (filter #(= (:name %1) "Ahmet") )
     (first)
     (:experience)
     (map #(->> mapExp2
                (:experiences)
                (filter (fn [m] (= (:id m) %)))
                (first)
                (:level)
                ))
     )

;;a01: Daha sonra o değişkendeki veriyi map fonksiyonuna gönder.
;;a02: O değişkendeki veriyi map fonksiyonuna değil, map'in içindeki anonim fonksiyona gönder.