(ns validators.spec-validator
  (:require [myapp.specs.domain]
            [myapp.specs.state]
            [myapp.specs.events]
            [malli.core :as m]
            [malli.generator :as mg]
            [malli.error :as me]))

(def spec-namespaces
  '[myapp.specs.domain
    myapp.specs.state
    myapp.specs.events])

(defn validate-spec-files []
  (let [errors (atom [])
        files  (atom [])]
    (doseq [ns-sym spec-namespaces]
      (require ns-sym :reload)
      (let [all-specs (ns-publics ns-sym)]
        (swap! files conj {:ns ns-sym :count (count all-specs)})
        (doseq [[name schema] all-specs]
          (try
            (m/schema @schema)
            (catch Throwable t
              (swap! errors conj
                     {:ns ns-sym
                      :schema name
                      :error (.getMessage t)}))))
        (doseq [[name schema] all-specs]
          (try
            (let [sample (mg/generate schema)]
              (when-not (m/validate schema sample)
                (swap! errors conj
                       {:ns ns-sym
                        :schema name
                        :sample sample
                        :errors (me/humanize (m/explain schema sample))})))
            (catch Throwable t
              (swap! errors conj
                     {:ns ns-sym
                      :schema name
                      :error (.getMessage t)}))))))

    (let [result (if (empty? @errors)
                   {:passed true :files @files}
                   {:passed false :files @files :errors @errors})]
      (when-not (:passed result)
        (binding [*out* *err*]
          (println "Spec validation failed:")
          (prn result))
        (throw (ex-info "Spec validation failed" result)))
      result)))
