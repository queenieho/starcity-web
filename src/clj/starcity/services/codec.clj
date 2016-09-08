(ns starcity.services.codec
  "Both `http-kit` and `ring.util.codec/form-encode` use an invalid (according
  to some, Stripe included) way of form-encoding arrays. This ns is copy of the
  aforementioned Ring implementation with a minor modification -- see line
  marked as such below.

  see also:
  http://stackoverflow.com/questions/6243051/how-to-pass-an-array-within-a-query-string"
  (:require [clojure.string :as str])
  (:import java.util.Map
           [java.net URLEncoder]))

(defprotocol ^:no-doc FormEncodeable
  (form-encode* [x encoding]))

(extend-protocol FormEncodeable
  String
  (form-encode* [unencoded encoding]
    (URLEncoder/encode unencoded encoding))
  Map
  (form-encode* [params encoding]
    (letfn [(encode [x] (form-encode* x encoding))
            ;; MODIFICATION HERE
            (encode-array-param [[k v]] (str (encode (name k)) "[]=" (encode v)))
            ;; ^^^^^^^^^^^^^^^^^^^^^^^^^
            (encode-param [[k v]] (str (encode (name k)) "=" (encode v)))]
      (->> params
           (mapcat
            (fn [[k v]]
              (if (or (seq? v) (sequential? v) )
                (map #(encode-array-param [k %]) v) ; AND HERE
                [(encode-param [k v])])))
           (str/join "&"))))
  Object
  (form-encode* [x encoding]
    (form-encode* (str x) encoding)))

(defn form-encode
  "Encode the supplied value into www-form-urlencoded format, often used in
  URL query strings and POST request bodies, using the specified encoding.
  If the encoding is not specified, it defaults to UTF-8"
  [x & [encoding]]
  (form-encode* x (or encoding "UTF-8")))
