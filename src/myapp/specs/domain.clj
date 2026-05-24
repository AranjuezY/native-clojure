(ns myapp.specs.domain
  (:require [malli.core :as m]))

(def TextValue
  (m/schema
   [:string {:min 0, :max 200}]))
