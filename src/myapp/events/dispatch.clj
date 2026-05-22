(ns myapp.events.dispatch)

(defmulti handle-event
  :event/type)
