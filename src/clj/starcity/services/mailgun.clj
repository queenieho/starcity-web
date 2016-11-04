(ns starcity.services.mailgun
  (:require [mailgun.mail :as mail]
            [starcity.config.mailgun :as config]
            [starcity.environment :refer [environment]]
            [mount.core :as mount :refer [defstate]]))

;; =============================================================================
;; API
;; =============================================================================

(defn send-email
  ([to subject content]
   (send-email to config/default-sender subject content))
  ([to from subject content]
   (let [creds {:key config/api-key :domain config/domain}]
     (mail/send-mail creds {:from    from
                            ;; TODO: Remove.
                            :to      (if (= environment :production) to "josh@joinstarcity.com")
                            :subject subject
                            :html    content}))))

(def default-signature
  [:p "Best,"
   [:br]
   [:br]
   "Mo"
   [:br]
   "Head of Community"])
