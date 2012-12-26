(ns livereload.main
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]]
        [livereload.config :only [cfg configs]]
        [clojure.java.io :only [resource]]
        [livereload.util :only [info to-int do-kill-if-prod public-ip response-file]]
        me.shenfeng.http.server)
  (:require [livereload.util :as tmpl]
            [clojure.string :as str])
  (:import me.shenfeng.livereload.FileWatcher))

(defonce server (atom nil))
(defonce watcher-thread (atom nil))
(def clients (atom {}))

(defn stop-server []
  (when-not (nil? @server)
    (info "shutdown live reload server....")
    (@server)
    (reset! server nil)))

(defasync polling-handler [req] cb
  )

(defwshandler ws-handler [req] con
  (swap! clients assoc con 1)
  (on-close con (fn [status]
                  (swap! clients dissoc con))))

(defn reload-js [req]
  {:status 200
   :headers {"Content-Type" "application/javascript"}
   :body (tmpl/reload-js {:server-host
                          (str (public-ip) ":" (cfg :port))})})

(defn file-hanlder [req]
  (or (response-file (subs (:uri req) 1))
      {:status 404
       :body (str (:uri req) " Not Found")}))

(defn handler [req]
  (case (:uri req)
    "/d/js" (reload-js req)
    "/d/polling" (polling-handler req)
    "/d/ws" (ws-handler req)
    "/d/doc" {:status 200
              :body (tmpl/documentation {:server-host
                                         (str (public-ip) ":" (cfg :port))})
              :headers {"Content-Type" "text/html"}}
    (file-hanlder req)))

(defn on-file-change [events]
  (let [events (map identity events)
        ignores (if-not (str/blank? (cfg :ignores))
                  (map re-pattern (str/split (cfg :ignores) #" "))
                  [])]
    (when (seq @clients)
      (let [b (if (> (count @clients) 1) "browsers" "browser")]
        (info "reload" (count @clients) b))
      (doseq [client (keys @clients)]
        (send-mesg client "reload")))))

(defn start-server []
  (stop-server)
  (do-kill-if-prod
   (reset! server (run-server handler {:port (cfg :port)
                                       :thread 1}))
   (when-not (nil? watcher-thread)
     (reset! watcher-thread
             (FileWatcher/start (cfg :root) on-file-change)))
   (info "server start"  (str "0.0.0.0:" (cfg :port)))
   (println (tmpl/welcome-msg
             {:root (.getAbsolutePath (java.io.File. ^String (cfg :root)))
              :server-host (str (public-ip) ":" (cfg :port))}))))

(defn -main [& args]
  (let [[options _ banner]
        (cli args
             ["-p" "--port" "Port to listen" :default 6767 :parse-fn to-int]
             ["-i" "--ignores" "Ignore pattens, seperate by ','" :default ""]
             ["-r" "--root" "File Server root path" :default "."]
             ["-s" "--script" "Script hook before reload browser, like preprocess html, compile sass"]
             ["-h" "--[no-]help" "Print this help"])]
    (when (:help options) (println banner) (System/exit 0))
    (swap! configs merge options)
    (start-server)))
