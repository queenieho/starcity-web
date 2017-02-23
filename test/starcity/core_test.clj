(ns starcity.core-test
  (:require
   [clojure.test :refer :all]
   [datomic.api :as d]
   [starcity.test.datomic :as db :refer [with-conn]]))

(use-fixtures :once db/conn-fixture)

(deftest this-test-gets-a-database
  (with-conn conn
    (is (not (nil? conn)))
    (is (not (nil? (d/db conn))))))
