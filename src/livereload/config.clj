(ns livereload.config

  )

(defonce configs (atom {}))

(defn cfg [key & [default]]
  (if-let [v (or (key @configs) default)]
    v
    (when-not (contains? @configs key)
      (throw (RuntimeException. (str "unknow config for key " (name key)))))))
