(ns mars.components.pane)

(defn pane [header content]
  [:div header content])

(defn header [title subtitle]
  [:header.pane-header
   [:div.pane-header__title
    [:h1.title.is-1 [:strong {:dangerouslySetInnerHTML {:__html title}}]]
    [:h3.subtitle.is-4 {:dangerouslySetInnerHTML {:__html subtitle}}]]])

(defn content [content]
  [:section.pane-content
   content])
