(ns datomic.e10)

; Konu: Predicates

(require '[datomic.client.api :as d])

(use '[datomic.e03 :only [conn] :as e03])

(use '[datomic.e04 :as e04])

(def db (d/db conn))


; Predicate kelimesinin Türkçe doğrudan karşılığı yüklem.
; Fakat kullanım anlamı şu: Doğru veya yanlış olabilen ifadeler için kullanılıyor.

; Başka bir deyişle boolean değer döndüren fonksiyonlar birer yüklemdir, bizim açımızdan.
; Ancak clojureda tüm data typelar booleana çevrilebilir.
; In Clojure nil and false are considered "false" and therefore we say they are both "falsey".
; Dolayısıyla herhangi bir data type dönen herhangi bir fonksiyonu,
; bir if ifadesine veya boolean değer bekleyen herhangi bir fonksiyona parametre olarak gönderebilirsin
; Polimorfizm (polymorphism): Statik typelarda her bir fonksiyonun bir signature dediğimiz beklediğimiz data typeları var.
; Eğer bu data typelardan biri değilse fonksiyon çağrısı hata verir.
; Ama eğer bu data typeların subclasslarıysa o zaman buna polimorfizm özelliği denir. Hata vermez uygun bir yönlendirme (dispatch) yapılır.

; Dolayısıyla aslında matematikteki "önerme" terimine denk geliyor.
; Herhangi bir ifade eğer içindeki değişkenlerin değerlerine bağlı olarak doğru veya yanlış olabiliyorsa, bu bir predicatetır.
; Dolayısıyla, biz predicate = önerme = yüklem gibi terimleri birbirine yerine kullanabiliriz.

; Bizim normal clojure fonksiyonlarımızı predicate olarak kullanabiliriz.

; Şu ana kadarki sorgularımızda sorgu kriterlerimizin bulunduğu terimlere "data pattern" diyorduk.
; Mesela:

(d/q
  '[:find (pull ?e [*])
    :where
    [?e :product/name "Kalem"]]
  db)

;=>
;[[{:db/id 74766790688850,
;   :product/name "Kalem",
;   :product/color #:db{:id 74766790688844, :ident :color/red},
;   :product/id 1}]
; [{:db/id 74766790688851,
;   :product/name "Kalem",
;   :product/color #:db{:id 74766790688846, :ident :color/blue},
;   :product/id 2}]]

; (.startsWith ?name "K")
; bunun Javadaki muadil sentaksı şöyle olur:
; name.startsWith("K")

(d/q
  '[:find (pull ?e [*])
    :where
    [?e :product/name ?name]
    [(= ?name "Kalem")]]
  db)

;=>
;[[{:db/id 74766790688850,
;   :product/name "Kalem",
;   :product/color #:db{:id 74766790688844, :ident :color/red},
;   :product/id 1}]
; [{:db/id 74766790688851,
;   :product/name "Kalem",
;   :product/color #:db{:id 74766790688846, :ident :color/blue},
;   :product/id 2}]]

; `.startsWith` bir clojure fonksiyonu
; `(.startsWith ?name "K")` bu ifadenin düz clojure kullanımı şöyle olurdu:

(def deneme "Bilgisayar")

(.startsWith deneme "B")
;=> true
(.startsWith deneme "K")
;=> false


; Başka bir örnek olarak `<` veya `>` gibi operatörleri ele alalım.
; Normalde bunları numerik sayıların kıyaslamasında kullanırız:

; Bu fonksiyonu da datomic sorguları içinde kullanabiliriz.
; Sorgu: 5'ten daha az adet sipariş edilen işlemlerin kayıtlarını getir.)

(d/q
  '[:find (pull ?order [*])
    :where
    [?order :order/size ?size]
    [(< ?size 5)]]
  db)

;=> [[{:db/id 79164837199959, :order/product #:db{:id 74766790688852}, :order/size 4}]]

; Not: SQL'de bunu kısmen yapabilirsiniz.
; Ancak eğer ilgili veritabanı sunucusunun built-in fonksiyonları kullanabilirsiniz.
; Fakat biz herhangi bir Clojure, Java fonksiyonunu kullanabiliriz.
; Dolayısıyla kendimiz de yeni fonksiyon tanımlayabiliriz.

; Değer döndüren ve pure (side-effect üretmeyen) tüm clojure fonksiyonlarını bu şekilde predicate gibi kullanabiliriz.
; Değer döndürmek: Herhangi bir şeyi return etmesi anlamına geliyor.
; Pure = saf fonksiyon ne demek?
; f(x) = x+3
; Matematikteki fonksiyonların tümü saftır.
; Yani o fonksiyonun sonucu (çıktısı), her zaman ve sadece girdiye bağlıdır.
; Mesela f(x) fonksiyonuna arg olarak 2 verirsek, her zaman 5 alırız.
; Bizim clj fonksiyonumuz verilen bir değer için her zaman aynı sonucu dönüyorsa, ona saf fonksiyon deriz.
; Not: Saf (pure) fonksiyonların ne olduğuna dair detaylı örneklerle anlatım için:
; Özet olarak, bir fonksiyonun sonucu sadece girdi argümanlarına bağlıysa buna saf fonksiyon denir.

; Çok basit bir saf olmayan fonksiyon örneği:

(def a 5)
(defn f [x]
  (+ x a))
(f 3)
;=> 8
(def a 8)
(f 3)
;=> 11

;; `(f 3) ` ifadesi önce 8 sonra 11 değerini döndürdü.
;; Demek ki, f saf olmayan bir fonksiyondur.

(def predicate <)
(predicate 7 8)
;=> true
(predicate 9 8)
;=> false

;Kendi tanımladığımız fonksiyonu nasıl sorguda kullanırız
;Qualified ns ile kullanmamız gerek
(d/q
  '[:find (pull ?order [*])
    :where
    [?order :order/size ?size]
    [(datomic.e10/predicate ?size 5)]]
  db)

;=> [[{:db/id 79164837199959, :order/product #:db{:id 74766790688852}, :order/size 4}]]