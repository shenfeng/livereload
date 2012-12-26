(ns livereload.util
  (:use [clojure.java.shell :only [sh]]
        [me.shenfeng.mustache :only [gen-tmpls-from-resources]]
        [livereload.config :only [cfg]])
  (:require [clojure.string :as str])
  (:import [java.net NetworkInterface InetAddress]
           [java.util Collections Date]
           java.text.SimpleDateFormat
           java.io.File))

(gen-tmpls-from-resources "templates" [".tpl" ".js"])

(defn info [& args]
  (let [s (SimpleDateFormat. "yyyy-MM-dd HH:mm:ss")
        args (concat [(.format s (Date.)) "-"] args)]
    (apply println args)))

(defn to-int [s] (cond
                  (string? s) (Integer/parseInt s)
                  (instance? Integer s) s
                  (instance? Long s) (.intValue ^Long s)
                  :else 0))

(defmacro do-kill-if-prod [& body]
  `(loop [i# 1]
     (let [pid# (str/trim (:out (sh "lsof"
                                    "-t" "-sTCP:LISTEN"
                                    (str "-i:" (cfg :port)))))]
       (when-not (str/blank? pid#)
         (info "kill pid" pid# i# "times, status" (:exit (sh "kill" pid#)))))
     (let [r# (try ~@body 1
                   (catch java.net.BindException e#
                     (if (> i# 30)    ; wait about 4.5s
                       (do
                         (info "giving up")
                         (throw e#))
                       (Thread/sleep 150))))]
       (when-not r#
         (recur (inc i#))))))

(defn public-ip []
  (some (fn [^String ip]
          (when (and (> 16 (count ip))
                     (not (.startsWith ip "127")))
            ip))
        (mapcat  (fn [i]
                   (map (fn [a]
                          (.getHostAddress ^InetAddress  a))
                        (Collections/list (.getInetAddresses ^NetworkInterface i))))
                 (Collections/list (NetworkInterface/getNetworkInterfaces)))))

;;; port from ring-clojure to exclude the dependency, since I want livereload to be small
(def default-mime-types
  {"7z"    "application/x-7z-compressed"
   "aac"   "audio/aac"
   "ai"    "application/postscript"
   "asc"   "text/plain"
   "atom"  "application/atom+xml"
   "avi"   "video/x-msvideo"
   "bin"   "application/octet-stream"
   "bmp"   "image/bmp"
   "bz2"   "application/x-bzip"
   "class" "application/octet-stream"
   "cer"   "application/pkix-cert"
   "crl"   "application/pkix-crl"
   "crt"   "application/x-x509-ca-cert"
   "css"   "text/css"
   "csv"   "text/csv"
   "deb"   "application/x-deb"
   "dll"   "application/octet-stream"
   "dmg"   "application/octet-stream"
   "dms"   "application/octet-stream"
   "doc"   "application/msword"
   "dvi"   "application/x-dvi"
   "eps"   "application/postscript"
   "etx"   "text/x-setext"
   "exe"   "application/octet-stream"
   "flv"   "video/x-flv"
   "flac"  "audio/flac"
   "gif"   "image/gif"
   "gz"    "application/gzip"
   "htm"   "text/html"
   "html"  "text/html"
   "ico"   "image/x-icon"
   "iso"   "application/x-iso9660-image"
   "jar"   "application/java-archive"
   "jpe"   "image/jpeg"
   "jpeg"  "image/jpeg"
   "jpg"   "image/jpeg"
   "js"    "text/javascript"
   "json"  "application/json"
   "lha"   "application/octet-stream"
   "lzh"   "application/octet-stream"
   "mov"   "video/quicktime"
   "m4v"   "video/mp4"
   "mp3"   "audio/mpeg"
   "mp4"   "video/mp4"
   "mpe"   "video/mpeg"
   "mpeg"  "video/mpeg"
   "mpg"   "video/mpeg"
   "oga"   "audio/ogg"
   "ogg"   "audio/ogg"
   "ogv"   "video/ogg"
   "pbm"   "image/x-portable-bitmap"
   "pdf"   "application/pdf"
   "pgm"   "image/x-portable-graymap"
   "png"   "image/png"
   "pnm"   "image/x-portable-anymap"
   "ppm"   "image/x-portable-pixmap"
   "ppt"   "application/vnd.ms-powerpoint"
   "ps"    "application/postscript"
   "qt"    "video/quicktime"
   "rar"   "application/x-rar-compressed"
   "ras"   "image/x-cmu-raster"
   "rb"    "text/plain"
   "rd"    "text/plain"
   "rss"   "application/rss+xml"
   "rtf"   "application/rtf"
   "sgm"   "text/sgml"
   "sgml"  "text/sgml"
   "svg"   "image/svg+xml"
   "swf"   "application/x-shockwave-flash"
   "tar"   "application/x-tar"
   "tif"   "image/tiff"
   "tiff"  "image/tiff"
   "txt"   "text/plain"
   "webm"  "video/webm"
   "wmv"   "video/x-ms-wmv"
   "xbm"   "image/x-xbitmap"
   "xls"   "application/vnd.ms-excel"
   "xml"   "text/xml"
   "xpm"   "image/x-xpixmap"
   "xwd"   "image/x-xwindowdump"
   "zip"   "application/zip"})

(defn- filename-ext
  "Returns the file extension of a filename or filepath."
  [filename]
  (second (re-find #"\.([^./\\]+)$" filename)))

(defn- find-index-file
  "Search the directory for an index file."
  [^File dir]
  (first
   (filter
    #(.startsWith (.toLowerCase (.getName ^File %)) "index.")
    (.listFiles dir))))

(defn- ^File get-file
  "Safely retrieve the correct file. See file-response for an
  explanation of options."
  [^String path]
  (let [^File file (File. ^String (cfg :root) path)]
    (cond
     (.isDirectory file) (or (find-index-file file) file)
     (.exists file) file)))

(defn- guess-mime-type
  "Returns a String corresponding to the guessed mime type for the given file,
  or application/octet-stream if a type cannot be guessed."
  [^File file]
  (or (default-mime-types (filename-ext (.getName file)))
      "text/plain"))

(defn- get-file-info [^File f]
  (let [name (.getName f)
        formater (SimpleDateFormat. "yyy-MM-dd HH:mm:ss")
        d (.isDirectory f)]
    {:name name
     :size (if-not d
             (let [size (.length f)]
               (cond (> size (* 1024 1024)) (format "%.2fM" (/ size 1024.0 1024))
                     (> size (* 1024)) (format "%.2fk" (/ size 1024.0))
                     :else size))
             "-")
     :mtime (let [mtime (.lastModified f)]
              (.format formater (Date. mtime)))
     :href (if d (str name "/") name)}))

(defn- get-dirs [path]
  (let [parts (str/split path #"/")]
    (map (fn [i]
           {:name (nth parts i)
            :href (str "/" (str/join "/" (take (inc i) parts)) "/")})
         (range 0 (count parts)))))

(defn response-file [path]
  (when-let [file (get-file path)]
    (if (.isDirectory file)
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (let [path (if (str/blank? path) "/" path)]
               (dir-list {:dir path
                          :dirs (get-dirs path)
                          :files (sort-by :name (map get-file-info (.listFiles file)))}))}
      {:status 200
       :headers {"Content-Length" (str (.length file))
                 "Content-Type" (guess-mime-type file)}
       :body file})))
