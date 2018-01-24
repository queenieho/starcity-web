(ns starcity.controllers.collaborate
  (:require [blueprints.models.events :as events]
            [bouncer
             [core :as b]
             [validators :as v]]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.util.validation :as validation]))


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
                  (facade/maybe-errors errors)
                  (facade/maybe-messages messages)))


(defn- view [req & {:as opts}]
  (common/page req {:main        (collaborate opts)
                    :css-bundles ["public.css"]
                    :js-bundles  ["main.js"]}))


;; =============================================================================
;; Handlers
;; =============================================================================


(defn submit!
  [{params :params :as req}]
  (let [vresult (validate params)]
    (if-let [{:keys [email type message]} (validation/valid? vresult)]
      (do
        @(d/transact-async conn [(events/create-collaborator email type message)])
        (common/render-ok (view req :messages ["Thanks! We'll be in touch soon."])))
      (common/render-malformed (view req :errors (validation/errors vresult))))))


(defn show
  "Show the 'Collaborate with Us' page."
  [req]
  (common/render-ok (view req)))
