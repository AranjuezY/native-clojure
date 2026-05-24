(ns myapp.ui.root
  (:require [myapp.ui.text :as text]))

(defn view
    "Root component: single-panel layout with a text input."
  [{:keys [text on-update-text]}]
  {:fx/type :stage
   :showing true
   :title "fx"
   :scene {:fx/type :scene
           :width 400
           :height 200
           :root {:fx/type :v-box
                  :alignment :center
                  :spacing 10
                  :children [{:fx/type :label
                              :text "Enter text:"}
                             {:fx/type text/text-input
                              :text text
                              :on-change on-update-text}]}}})
