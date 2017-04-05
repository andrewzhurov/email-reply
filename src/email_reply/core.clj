(ns email-reply.core
  (:require [clojure.string :as str]))

(def tokens 
  (array-map  :signature #"^Sent from .+"
              :reply-metadata #"On[\w \d\n,:<@.>]*wrote:"
              :quoted #">+.*"
              :text-line #".+"
              ))
(def regex-tokens (->> tokens
                       vals
                       (str/join "|")
                       re-pattern))
(defn- obtain-type [token]
  (some (fn [[k v]] (if (re-matches v token)
                        {:type k :value token} nil))
        tokens))

(defn- parse-typed 
  "Parses to typed tokens"
  [string]
  (->> string
       (re-seq regex-tokens)
       (map obtain-type)))

(defn email->last-reply 
  "Parses and returns the respond body text"
  [email]
  (->> email
       parse-typed
       (filter #(= :text-line (:type %)))
       (mapcat :value)
       (apply str)))

