(ns starcity.controllers.collaborate
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [datomic.api :as d]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.models.cmd :as cmd]
            [starcity.util.validation :as validation]
            [starcity.views.base :as base]))

;; =============================================================================
;; Helpers
;; =============================================================================

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

;; =============================================================================
;; Views
;; =============================================================================

(html/defsnippet collaborate "templates/collaborate.html" [:main]
  [{:keys [errors messages]}]
  [:div.alerts] (if errors
                 (base/maybe-errors errors)
                 (base/maybe-messages messages)))

(defn- view [req & {:as opts}]
  (base/public-base req :main (collaborate opts)))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn submit!
  [{params :params :as req}]
  (let [vresult (validate params)]
    (if-let [{:keys [email type message]} (validation/valid? vresult)]
      (do
        (d/transact conn [(cmd/add-collaborator email type message)])
        (common/render-ok (view req :messages ["Thanks! We'll be in touch soon."])))
      (common/render-malformed (view req :errors (validation/errors vresult))))))

(defn show
  "Show the 'Collaborate with Us' page."
  [req]
  (common/render-ok (view req)))
