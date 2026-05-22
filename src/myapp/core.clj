(ns myapp.core
  (:require [cljfx.api :as fx]
            [myapp.events.dispatch :refer [handle-event]]
            [myapp.ui.root :as ui.root])
  (:gen-class))

(defn initial-state [])

(defonce *state
  (atom (initial-state)))

(defn event-handler [event]
  (swap! *state handle-event event))

(def renderer
  (fx/create-renderer
    :middleware
    (fx/wrap-map-desc
      (fn [state]
        (ui.root/view
          (assoc state
                 :on-update-text
                 #(event-handler {})))))))

(defn -main [& _args]
  (fx/mount-renderer *state renderer))

