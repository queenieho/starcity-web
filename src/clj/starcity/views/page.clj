(ns starcity.views.page
  (:require [starcity.views.components
             [head :as head]
             [footer :as f]
             [navbar :as n]
             [notification :as nf]
             [layout :as l]]
            [optimus.html :as optimus]
            [hiccup.page :refer [html5
                                 include-css
                                 include-js]]
            [cheshire.core :as json]
            [clojure.spec :as s]
            [starcity.web.messages :as msg]))

;; =============================================================================
;; Constants
;; =============================================================================

(def ^:private google-analytics
  [:script
   "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-81813253-1', 'auto');
  ga('send', 'pageview');"])

(def ^:private jquery
  "https://code.jquery.com/jquery-2.1.1.min.js")

(def ^:private base-css-bundle
  "styles.css")

(def ^:private base-js-bundle
  "main.js")

;; =============================================================================
;; Components
;; =============================================================================

;;; NOTE: With the exception of `title`, these are all terrible.

(defn title
  "Construct the page title."
  [t]
  (str "Starcity &mdash; " t))

(defn- onboarding-auth-item [{:keys [context] :as req}]
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

;; =====================================
;; Navbars

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

;; =============================================================================
;; Includes
;; =============================================================================

(defn- include-json
  [json]
  (for [[name obj] json]
    [:script
     (format "var %s = %s;" name (json/encode obj))]))

(defn scripts
  "Specify _paths_ to additional `scripts` to be loaded into the page (in order
  provided)."
  [& scripts]
  {::scripts scripts})

(defn bundles
  "Specify names of Optimus `bundles` to be loaded into the page (in order
  provided). Bundles are loaded _after_ `scripts.`"
  [& bundles]
  {::bundles bundles})

(defn json
  [& json]
  {::json (for [[object-name object-or-thunk :as j] json]
            (if (fn? object-or-thunk)
              [object-name (object-or-thunk)]
              j))})

(defn css [& css]
  {::css css})

(defn- render-html-content [req content]
  (for [c content]
    (if (fn? c)
      (c req)
      c)))

(defn- render-head [req title css-includes]
  (->> (concat css-includes [base-css-bundle])
       (optimus/link-to-css-bundles req)
       (apply head/head title)))

(defn- parse-content
  "Separates _includes_ from HTML content."
  [& content]
  (let [grp (group-by map? content)]
    [(get grp false)
     (or (->> (get grp true) (apply (partial merge-with concat))) {})]))

(defn page
  "Page template with a solid navbar."
  [title & content]
  (let [[content includes] (apply parse-content content)]
    (fn [req]
      (html5
       {:lang "en"}
       (render-head req title (includes ::css))
       [:body
        (render-html-content req content)
        footer
        (include-json (includes ::json))
        (include-js jquery)
        (apply include-js (includes ::scripts))
        (optimus/link-to-js-bundles req (concat (includes ::bundles)
                                                [base-js-bundle]))
        google-analytics]))))

(defn app
  "Produce a ClojureScript app handler with configurable script, stylesheet and
  content injection."
  [app-name title & content]
  (let [[content includes] (apply parse-content content)]
    (fn [req]
      (html5
       {:lang "en"}
       (render-head req title (includes ::css))
       [:body
        (render-html-content req content)
        (include-json (includes ::json))
        (apply include-js (includes ::scripts))
        (optimus/link-to-js-bundles req (concat (includes ::bundles)
                                                [(str app-name ".js")]))
        [:script (format "window.onload = function() { %s.core.run(); }" app-name)]]))))
