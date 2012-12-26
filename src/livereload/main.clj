(ns livereload.main
  (:gen-class)
  (:use [clojure.tools.cli :only [cli]]
        [livereload.config :only [cfg configs]]
        [clojure.java.io :only [resource]]
        [livereload.util :only [info to-int do-kill-if-prod public-ip response-file]]
        me.shenfeng.http.server)
  (:require [livereload.util :as tmpl]))

(defonce server (atom nil))
(defonce watcher-thread (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    (info "shutdown Rssminer server....")
    (@server)
    (reset! server nil)))

(defasync polling-handler [req] cb
  )

(defwshandler ws-handler [req] con
  )

(defn reload-js [req]
  {:status 200
   :headers {"Content-Type" "application/javascript"}
   :body (tmpl/reload-js)})

(defn file-hanlder [req]
  (or (response-file (subs (:uri req) 1))
      {:status 404
       :body (str (:uri req) " Not Found")}))

(defn handler [req]
  (case (:uri req)
    "/d/js" (reload-js req)
    "/d/polling" (polling-handler req)
    "/d/ws" (ws-handler)
    (file-hanlder req)))

(defn start-server []
  (stop-server)
  (do-kill-if-prod
   (reset! server (run-server handler {:port (cfg :port)
                                       :thread 1}))
   (when-not (nil? watcher-thread)
     (reset! watcher-thread nil))
   (println "server start"  (str "0.0.0.0:" (cfg :port)))))

(defn -main [& args]
  (let [[options _ banner]
        (cli args
             ["-p" "--port" "Port to listen" :default 6767 :parse-fn to-int]
             ["-i" "--ignore" "Ignore pattens, seperate by ','"]
             ["-s" "--script" "Script hook before reload browser, like preprocess html, compile sass"]
             ["-h" "--[no-]help" "Print this help"])]
    (when (:help options) (println banner) (System/exit 0))
    (swap! configs merge options)
    (start-server)))
