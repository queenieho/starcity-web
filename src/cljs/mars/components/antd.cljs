(ns mars.components.antd
  (:require [cljsjs.antd]
            [reagent.core :as r]))

(def ^:private antd js/window.antd)

(def button (r/adapt-react-class (.-Button antd)))
(def card (r/adapt-react-class (.-Card antd)))
(def dropdown (r/adapt-react-class (.-Dropdown antd)))
(def icon (r/adapt-react-class (.-Icon antd)))

;; =============================================================================
;; Menu

(def ^:private menu* (.-Menu antd))
(def menu (r/adapt-react-class menu*))
(def sub-menu (r/adapt-react-class (.-SubMenu menu*)))
(def menu-item (r/adapt-react-class (.-Item menu*)))

;; =============================================================================
;; Tabs

(def ^:private tabs* (.-Tabs antd))
(def tabs (r/adapt-react-class tabs*))
(def tab-pane (r/adapt-react-class (.-TabPane tabs*)))

;; =============================================================================
;; Steps

(def ^:private steps* (.-Steps antd))
(def steps (r/adapt-react-class steps*))
(def step (r/adapt-react-class (.-Step steps*)))

;; =============================================================================
;; Form

(def ^:private form* (.-Form antd))
(def form (r/adapt-react-class form*))
(def form-item (r/adapt-react-class (.-Item form*)))

;; =============================================================================
;; Input

(def input (r/adapt-react-class (.-Input antd)))

;; =============================================================================
;; Checkbox

(def checkbox (r/adapt-react-class (.-Checkbox antd)))

;; =============================================================================
;; Tooltip

(def tooltip (r/adapt-react-class (.-Tooltip antd)))

;; =============================================================================
;; Timeline

(def ^:private timeline* (.-Timeline antd))
(def timeline (r/adapt-react-class timeline*))
(def timeline-item (r/adapt-react-class (.-Item timeline*)))

;; =============================================================================
;; Modal

(def modal (r/adapt-react-class (.-Modal antd)))
