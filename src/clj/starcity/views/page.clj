(ns starcity.views.page
  (:require [starcity.views.components
             [head :refer [head]]
             [footer :as f]
             [navbar :as n]
             [notification :as nf]
             [layout :as l]]
            [hiccup.page :refer [html5 include-css include-js]]
            [cheshire.core :as json]
            [clojure.spec :as s]
            [starcity.web.messages :as msg]))

;; =============================================================================
;; Constants
;; =============================================================================

(def google-analytics
  [:script
   "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-81813253-1', 'auto');
  ga('send', 'pageview');"])

(def base-js
  ["https://code.jquery.com/jquery-2.1.1.min.js"])

(def base-css
  "/assets/css/starcity.css")

;; =============================================================================
;; Internal
;; =============================================================================

(defn- include-json
  [json]
  (for [[name obj] json]
    [:script
     (format "var %s = %s" name (json/encode obj))]))

(defn- scripts? [content]
  (and (map? content) (:scripts content)))

(defn- json? [content]
  (and (map? content) (:json content)))

(defn- onboarding-auth-item [{:keys [context]}]
  (cond
    (nil? context)                      ["/onboarding" "Security Deposit"]
    (re-find #"^/onboarding.*" context) ["/settings" "Account"]
    :otherwise                          ["/onboarding" "Security Deposit"]))

(defn- auth-item [req]
  (let [role          (get-in req [:identity :account/role])
        [uri content] (case role
                        :account.role/applicant  ["/apply" "Resume Application"]
                        :account.role/onboarding (onboarding-auth-item req)
                        :account.role/admin      ["/admin" "Admin"]
                        ["/login" "Log In"])]
    (n/nav-item uri content :button)))

;; =============================================================================
;; API
;; =============================================================================

(defn title [t] (str "Starcity &mdash; " t))

(defn content [& content]
  (fn [req]
    (for [c content]
      (if (fn? c)
        (c req)
        c))))

(defn navbar [req]
  (n/navbar
   false
   (n/nav-item "/communities" "Communities")
   (n/nav-item "/faq" "FAQ")
   (n/nav-item "/about" "About")
   (auth-item req)))

(defn navbar-inverse [req]
  (n/navbar
   true
   (n/nav-item "/communities" "Communities")
   (n/nav-item "/faq" "FAQ")
   (n/nav-item "/about" "About")
   (auth-item req)))

(defn messages [req]
  (let [errors  (msg/errors-from req)
        success (msg/success-from req)]
    (l/section
     {:style (if (empty? (concat errors success))
               "display: none;"
               "padding-bottom: 0;")}
     (l/container
      (for [e errors] (nf/danger e))
      (for [s success] (nf/success s))))))

;; for convenience when constructing pages
(def footer f/footer)

(defn scripts [& scripts]
  {:scripts scripts})

(defn json [& json]
  {:json (for [[object-name object-or-thunk :as j] json]
           (if (fn? object-or-thunk)
             [object-name (object-or-thunk)]
             j))})

;; =============================================================================
;; Page Constructors

;; TODO: Remove repetetive bits in the two functions below.

(defn page
  "Page template with a solid navbar."
  [title & content]
  (let [scripts (->> (filter scripts? content) (mapcat :scripts))
        json    (->> (filter json? content) (mapcat :json))
        content (remove #(or (scripts? %) (json? %)) content)]
    (fn [req]
      (html5
       {:lang "en"}
       (head title base-css)
       [:body
        (for [c content]
          (if (fn? c)
            (c req)
            c))
        footer
        (include-json json)
        (apply include-js (concat base-js scripts ["/js/main.js"]))
        google-analytics]))))

(defn cljs-page
  [app-name title & content]
  (let [scripts (->> (filter scripts? content) (mapcat :scripts))
        json    (->> (filter json? content) (mapcat :json))]
    (html5
     {:lang "en"}
     (head title base-css)
     [:body
      (remove #(or (scripts? %) (json? %)) content)
      (include-json json)
      (apply include-js (concat scripts
                                [(format "/js/cljs/%s.js" app-name)]))
      [:script (format "window.onload = function() { %s.core.run(); }" app-name)]])))
