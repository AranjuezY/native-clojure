(ns myapp.ui.text)

(defn text-input
  "Text input component.
   Props:
     - :text  - current text value from AppState
     - :on-change - callback to dispatch :update-text event"
  [{:keys [text on-change]}]
  {:fx/type :text-field
   :text text
   :on-text-changed on-change})
