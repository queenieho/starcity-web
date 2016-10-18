(ns starcity.views.components.modal)

;; =============================================================================
;; Simple Modal

(defn simple [& content]
  [:div.modal
   [:div.modal-background]
   [:div.modal-container
    [:div.modal-content content]]
   [:button.modal-close]])

;; =============================================================================
;; Modal Card

(defn head [title]
  [:header.modal-card-head
   [:p.modal-card-title title]
   [:button.delete]])

(defn body
  [& content]
  [:section.modal-card-body content])

(defn foot
  [& content]
  [:footer.modal-card-foot content])

(defn card
  [& content]
  [:div.modal
   [:div.modal-background]
   [:div.modal-card
    content]])
