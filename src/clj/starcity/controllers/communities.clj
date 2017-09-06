(ns starcity.controllers.communities
  (:require [facade
             [core :as facade]
             [snippets :as snippets]]
            [net.cgrand.enlive-html :as html]
            [taoensso.timbre :as timbre]
            [starcity.controllers.common :as common]))

;; =============================================================================
;; Views
;; =============================================================================

(def ^:private mission-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600|Rock+Salt")

(html/defsnippet mission-svg "templates/mission/svg.html" [:svg] [])
(html/defsnippet mission-main "templates/mission.html" [:main] [])

(def ^:private soma-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600|Vast+Shadow")

(html/defsnippet soma-svg "templates/soma/svg.html" [:svg] [])
(html/defsnippet soma-main "templates/soma.html" [:main] [])

(def ^:private coming-soon-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600|Vast+Shadow")

(html/defsnippet coming-soon-svg "templates/soma/svg.html" [:svg] [])
(html/defsnippet coming-soon-main "templates/coming-soon.html" [:main]
  [{:keys [errors messages]}]
  [:div.alerts] (cond
                  errors (facade/maybe-errors errors)
                  messages (facade/maybe-messages messages)))

;; =============================================================================
;; Handlers
;; =============================================================================

(defn show-mission
  "Show the Mission community page."
  [req]
  (->> (facade/public req
                      :svg (mission-svg)
                      :header (snippets/public-header :communities)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"]
                      :main (mission-main)
                      :fonts [mission-fonts])
       (common/render-ok)))

(defn show-soma
  "Show the SoMa community page."
  [req]
  (->> (facade/public req
                      :svg (soma-svg)
                      :header (snippets/public-header :communities)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"]
                      :main (soma-main)
                      :fonts [soma-fonts])
       (common/render-ok)))

(defn show-coming-soon
  "Show the Coming Soon page, with a preview of new communities in our pipeline."
  [req & {:as opts}]
  (->> (facade/public req
                      :svg (coming-soon-svg)
                      :header (snippets/public-header :communities)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"]
                      :main (coming-soon-main opts)
                      :fonts [coming-soon-fonts])
       (common/render-ok)))

(defn- validate [params]
  (timbre/warn params)
  true)


(defn submit-suggestions!
  [{params :params :as req}]
  (let [vresult (validate params)]
    (if (= vresult true)
      (do
        ;;@(d/transact-async conn [(events/submit-city-suggestions params)])
        (show-coming-soon req :messages ["Thanks! We appreciate the feedback."]))
      (show-coming-soon req :errors ["Please select some cities!"]))))
