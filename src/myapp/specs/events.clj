(ns myapp.specs.events
  (:require [malli.core :as m]))

(def UpdateText
  (m/schema
   [:map
    [:event-id [:= :update-text]]
    [:value :string]]))
