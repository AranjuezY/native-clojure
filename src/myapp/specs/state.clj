(ns myapp.specs.state
  (:require [malli.core :as m]))

(def AppState
  (m/schema
   [:map
    [:text :string]]))
