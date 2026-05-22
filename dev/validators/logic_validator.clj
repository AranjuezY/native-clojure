(ns validators.logic-validator
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.namespace.find :as ns-find]
            [myapp.events.dispatch]
            [myapp.specs.state :refer [AppState]]
            [malli.core :as m]
            [malli.error :as me]
            [malli.generator :as mg]))

(def events-root-ns "myapp.events")

(def excluded-event-ns
  #{'myapp.events.dispatch
    'myapp.events.middleware})

(defn event-namespace? [ns-sym]
  (and (symbol? ns-sym)
       (str/starts-with? (str ns-sym) (str events-root-ns "."))
       (not (contains? excluded-event-ns ns-sym))))

(defn discover-event-namespaces []
  (->> (ns-find/find-namespaces-in-dir (io/file "src/myapp/events"))
       (filter event-namespace?)
       sort
       vec))

(defn load-event-namespaces! [ns-syms]
  (require 'myapp.events.dispatch :reload)
  (doseq [ns-sym ns-syms]
    (require ns-sym :reload))
  ns-syms)

(defn collect-event-types []
  (->> (methods myapp.events.dispatch/handle-event)
       keys
       (remove #{:default})
       sort
       vec))

(defn validate-event-smoke
  [event-type errors]
  (let [state (mg/generate AppState)
        event {:event/type event-type}]
    (try
      (let [new-state (myapp.events.dispatch/handle-event event state)]
        (when-not (m/validate AppState new-state)
          (swap! errors conj
                 {:event/type event-type
                  :input-state state
                  :input-event event
                  :output-state new-state
                  :error :invalid-app-state
                  :errors (me/humanize (m/explain AppState new-state))})))
      (catch Throwable t
        (swap! errors conj
               {:event/type event-type
                :input-state state
                :input-event event
                :error :exception
                :message (.getMessage t)})))))

(defn validate-event-logic []
  (let [errors (atom [])
        event-namespaces (discover-event-namespaces)
        _ (load-event-namespaces! event-namespaces)
        event-types (collect-event-types)]
    (doseq [event-type event-types]
      (validate-event-smoke event-type errors))
    (let [result (if (empty? @errors)
                   {:passed true
                    :event-types event-types
                    :count (count event-types)}
                   {:passed false
                    :event-types event-types
                    :count (count event-types)
                    :errors @errors})]
      (when-not (:passed result)
        (binding [*out* *err*]
          (println "Logic validation failed:")
          (prn result))
        (throw (ex-info "Logic validation failed" result)))
      result)))
