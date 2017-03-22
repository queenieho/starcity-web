(ns starcity.controllers.collaborate
  (:require [selmer.parser :as selmer]
            [starcity.datomic :refer [conn]]
            [starcity.views.common :refer [public-defaults]]
            [bouncer.core :as b]
            [starcity.util.validation :as validation]
            [bouncer.validators :as v]
            [datomic.api :as d]
            [starcity.models.cmd :as cmd]
            [starcity.controllers.common :as common]))

(def ^:private view (partial selmer/render-file "collaborate.html"))

(def ^:private collaborator-types
  #{"real-estate" "community-stakeholder" "vendor" "investor"})

(defn- validate [params]
  (b/validate
   params
   {:type    [[v/required :message "Please choose a collaborator type."]
              [v/member collaborator-types :message "The selected collaborator type is invalid."]]
    :email   [[v/required :message "Please enter your email address."]
              [v/email :message "Please enter a valid email address."]]
    :message [[v/required :message "Please enter a message."]]}))

(defn submit!
  [{params :params :as req}]
  (let [vresult (validate params)]
    (if-let [{:keys [email type message]} (validation/valid? vresult)]
      (do
        (d/transact conn [(cmd/add-collaborator email type message)])
        (common/ok (view (assoc (public-defaults req) :message "Thanks! We'll be in touch soon."))))
      (common/malformed (view (assoc (public-defaults req) :errors (validation/errors vresult)))))))

(defn show
  "Show the 'Collaborate with Us' page."
  [req]
  (common/ok (view (public-defaults req))))
