(ns starcity.api.mars.settings
  (:require [compojure.core :refer [defroutes POST]]
            [starcity.datomic :refer [conn]]
            [starcity.auth :as auth]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [starcity.models.account :as account]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :as string]
            [toolbelt.core :as tb]
            [starcity.util.validation :as uv]
            [starcity.util.response :as response]
            [datomic.api :as d]))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn- validate-password-params
  [params account]
  (letfn [(matching-password? [password]
            (account/is-password? account password))
          (same-passwords? [_]
            (= (:password-1 params) (:password-2 params)))]
    (b/validate
     params
     {:old-password [[v/required :message "You must enter your current password."]
                     [matching-password? :message "That is not the correct current password."]]
      :password-1   [[v/required :message "You must enter a new password."]
                     [v/min-count 8 :message "Your password should be at least 8 characters long."]
                     [same-passwords? :message "The passwords you entered don't match."]]})))

(defn- scrub-password-params
  [params]
  (tb/transform-when-key-exists params
    {:password-1 string/trim
     :password-2 string/trim}))

(defn change-password!
  [conn account params]
  (let [params  (scrub-password-params params)
        vresult (validate-password-params params account)]
    (if-not (uv/valid? vresult)
      (response/transit-malformed {:message (first (uv/errors vresult))})
      (do
        (account/change-password account (:password-1 params))
        (response/transit-ok {:result "ok"})))))

(s/fdef change-password!
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :params (s/keys :req-un [::old-password
                                              ::password-1
                                              ::password-2]))
        :ret (s/keys :req-un [::status ::body ::headers]))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes
  (POST "/change-password" []
        (fn [{params :params :as req}]
          (let [requester (auth/requester req)]
            (change-password! conn requester params)))))
