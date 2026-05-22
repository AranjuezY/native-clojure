(ns dev.validators.component-validator
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.walk :as walk]
            [malli.generator :as mg]))

(def components-root-ns "myapp.components")
(def excluded-component-ns #{'myapp.components.root})
(def required-map-keys #{:fx/type})

(defn component-namespace? [ns-sym]
  (and (symbol? ns-sym)
       (str/starts-with? (str ns-sym) (str components-root-ns "."))
       (not (contains? excluded-component-ns ns-sym))))

(defn discover-component-namespaces []
  (->> (ns-find/find-namespaces-in-dir (io/file "src/myapp/components"))
       (filter component-namespace?)
       sort
       vec))

(defn component-var? [v]
  (and (var? v)
       (fn? @v)
       (:component (meta v))))

(defn discover-component-vars [ns-syms]
  (mapcat
   (fn [ns-sym]
     (require ns-sym)
     (->> (ns-publics ns-sym)
          vals
          (filter component-var?)
          (mapv (fn [v]
                  {:ns ns-sym
                   :name (:name (meta v))
                   :var v}))))
   ns-syms))

(defn component-name [component-var]
  (str (:ns (meta component-var)) "/" (:name (meta component-var))))

(defn extract-component-props-spec [component-var]
  (:props-spec (meta component-var)))

(defn generate-sample-props [component-var]
  (let [props-spec (extract-component-props-spec component-var)]
    (cond
      (nil? props-spec)
      {:ok true
       :sample-props {}}

      :else
      (try
        {:ok true
         :sample-props (mg/generate props-spec)}
        (catch Throwable t
          {:ok false
           :error (.getMessage t)})))))

(defn render-component [component-var props]
  (@component-var props))

(defn find-invalid-event-handlers [tree]
  (let [hits (volatile! [])]
    (walk/prewalk
     (fn [node]
       (when (map? node)
         (doseq [[k v] node
                 :when (and (keyword? k)
                            (str/starts-with? (name k) "on-")
                            (not (map? v)))]
           (vswap! hits conj
                   {:key k
                    :value-type (some-> v type str)})))
       node)
     tree)
    @hits))

(defn validate-stable-render [component-var sample-props]
  (let [name (component-name component-var)]
    (try
      (let [results (repeatedly 3 #(render-component component-var sample-props))]
        (when-not (apply = results)
          {:component name
           :violation :unstable-render
           :sample-props sample-props
           :message "相同 props 多次渲染结果不一致"}))
      (catch Throwable t
        {:component name
         :violation :render-exception
         :sample-props sample-props
         :error (.getMessage t)
         :message "组件渲染抛出异常"}))))

(defn validate-desc-shape [component-var sample-props]
  (let [name (component-name component-var)]
    (try
      (let [desc (render-component component-var sample-props)]
        (cond
          (not (map? desc))
          {:component name
           :violation :not-a-map
           :actual-type (some-> desc type str)
           :message "组件返回值必须是 map"}

          (not-every? #(contains? desc %) required-map-keys)
          {:component name
           :violation :missing-required-keys
           :required required-map-keys
           :returned-keys (vec (keys desc))
           :message "组件返回的 desc map 缺少必须键"}

          :else
          (when-let [invalid (seq (find-invalid-event-handlers desc))]
            {:component name
             :violation :invalid-event-handler
             :occurrences (mapv #(select-keys % [:key :value-type]) invalid)
             :message ":on-* 的值必须是 event map"})))
      (catch Throwable t
        {:component name
         :violation :render-exception
         :sample-props sample-props
         :error (.getMessage t)
         :message "组件渲染抛出异常"}))))

(defn validate-component-var [component-var]
  (let [name          (component-name component-var)
        sample-result (generate-sample-props component-var)]
    (if-not (:ok sample-result)
      [{:component name
        :violation :sample-generation-failed
        :error (:error sample-result)
        :message "无法生成样例 props"}]
      (let [sample-props (:sample-props sample-result)]
        (->> [(validate-stable-render component-var sample-props)
              (validate-desc-shape component-var sample-props)]
             (remove nil?)
             vec)))))

(defn validate-components []
  (let [component-namespaces (discover-component-namespaces)
        all-vars             (vec (discover-component-vars component-namespaces))
        errors               (->> all-vars
                                  (mapcat #(validate-component-var (:var %)))
                                  vec)
        result               (cond-> {:passed (empty? errors)
                                      :component-namespaces component-namespaces
                                      :component-count (count all-vars)}
                               (seq errors) (assoc :errors errors))]
    (when-not (:passed result)
      (binding [*out* *err*]
        (println "Component validation failed:")
        (prn result))
      (throw (ex-info "Component validation failed" result)))
    result))
