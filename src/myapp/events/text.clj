(ns myapp.events.text
  (:require [myapp.events.dispatch :refer [handle-event]]))

(defmethod handle-event :update-text
  [event state]
  (if (contains? event :value)
    (assoc state :text (:value event))
    state))

(handle-event {:event/type :update-text :value "abc"}
              {:text "old" :cursor 3})
