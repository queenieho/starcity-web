(ns mars.account.rent.link-account.authorize.db
  (:require [cljs-time.coerce :as c]
            [cljs-time.core :as t]
            [cljs.spec :as s]
            [starcity.log :as l]))

(s/def ::rent-amount (s/and number? pos?))
(s/def ::commencement t/date?)
(s/def ::plan-start t/date?)
(s/def ::term (s/and integer? pos?))
(s/def ::amount-due number?)
(s/def ::plan-data
  (s/keys :req-un [::rent-amount
                   ::commencement
                   ::plan-start
                   ::term
                   ::amount-due]))

(s/def ::plan
  (s/or :empty-map (s/and empty? map?)
        :plan ::plan-data))

(s/def ::authorized boolean?)
(s/def ::subscribing boolean?)

(s/def ::db
  (s/keys :req-un [::plan ::authorized ::subscribing]))

(def path ::authorize)
(def default-value
  {path {:plan        {}
         :authorized  false
         :subscribing false}})

(defn set-plan [db plan-data]
  (let [res (-> (update plan-data :commencement c/from-string)
                (update :end-date c/from-string)
                (update :plan-start c/from-string))]
    (assoc db :plan res)))

(s/fdef set-plan
        :args (s/cat :db ::db :data map?)
        :ret ::db)

(defn rent-amount [db]
  (get-in db [:plan :rent-amount]))

(s/fdef rent-amount
        :args (s/cat :db ::db)
        :ret ::rent-amount)

(defn commencement-date [db]
  (get-in db [:plan :commencement]))

(s/fdef commencement-date
        :args (s/cat :db ::db)
        :ret t/date?)

(defn end-date [db]
  (get-in db [:plan :end-date]))

(s/fdef end-date
        :args (s/cat :db ::db)
        :ret t/date?)

(defn start-date [db]
  (get-in db [:plan :plan-start]))

(s/fdef start-date
        :args (s/cat :db ::db)
        :ret t/date?)

(defn term [db]
  (get-in db [:plan :term]))

(s/fdef term
        :args (s/cat :db ::db)
        :ret ::term)

(defn toggle-authorized [db]
  (update db :authorized not))

(defn authorized? [db]
  (:authorized db))

;;; Subscribe

(defn toggle-subscribing [db]
  (update db :subscribing not))

(defn subscribing? [db]
  (:subscribing db))
