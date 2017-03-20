(ns starcity.views.common
  (:require [optimus.link :as link]))

(defn public-defaults
  [req]
  {:head    {:stylesheets (link/bundle-paths req ["public.css"])}
   :scripts (link/bundle-paths req ["main.js"])})
