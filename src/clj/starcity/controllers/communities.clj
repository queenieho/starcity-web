(ns starcity.controllers.communities
  (:require [blueprints.models.suggestion :as suggestion]
            [clojure.string :as string]
            [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [toolbelt.datomic :as td]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]))

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

(def ^:private north-beach-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600|IM+Fell+DW+Pica+SC|Marcellus+SC")

(html/defsnippet north-beach-svg "templates/north-beach/svg.html" [:svg] [])
(html/defsnippet north-beach-main "templates/north-beach.html" [:main] [])

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
  (->> (common/page req {:svg         (mission-svg)
                         :header      (common/header :communities)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]
                         :main        (mission-main)
                         :fonts       [mission-fonts]})
       (common/render-ok)))

(defn show-soma
  "Show the SoMa community page."
  [req]
  (->> (common/page req {:svg         (soma-svg)
                         :header      (common/header :communities)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]
                         :main        (soma-main)
                         :fonts       [soma-fonts]})
       (common/render-ok)))

(defn show-north-beach
  "Show the North Beach community page."
  [req]
  (->> (common/page req {:svg         (north-beach-svg)
                         :header      (common/header :communities)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js" "styles.js" "north-beach.js"]
                         :scripts     ["https://cdn.jsdelivr.net/npm/snapsvg-cjs@0.0.6/dist/snap.svg.js"
                                       "https://cdn.jsdelivr.net/npm/linea@3.1.1/dist/minified-linea/linea-min.js"]
                         :main        (north-beach-main)
                         :fonts       [north-beach-fonts]})
       (common/render-ok)))

(defn show-coming-soon
  "Show the Coming Soon page, with a preview of new communities in our pipeline."
  [req & {:as opts}]
  (->> (common/page req {:svg         (coming-soon-svg)
                         :header      (common/header :communities)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]
                         :main        (coming-soon-main opts)
                         :fonts       [coming-soon-fonts]})
       (common/render-ok)))


(defn- validate-suggestions [params]
  (not (and (= 1 (count params)) (string/blank? (:other params)))))


(defn params->tx
  [{:keys [other] :as params}]
  (let [cities (->> (dissoc params :other) (keys) (map name))]
    (-> (if (not (string/blank? other))
          (concat cities (string/split other #","))
          cities)
        (suggestion/create-many))))


(defn submit-suggestions!
  [{params :params :as req}]
  (let [vresult (validate-suggestions params)]
    (if vresult
      (do
        @(d/transact-async conn (params->tx params))
        (show-coming-soon req :messages ["Thanks! We appreciate the feedback."]))
      (show-coming-soon req :errors ["Please select some cities!"]))))
