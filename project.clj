(defproject livereload "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [me.shenfeng/http-kit "1.2"]
                 [me.shenfeng/mustache "1.1"]
                 [org.clojure/tools.cli "0.2.2"]]
  :aot [livereload.main]
  :main livereload.main
  :uberjar-name "livereload.jar"
  :uberjar-exclusions [#".+\.java$" #".+\.clj$" #"pom.xml"]
  :plugins [[lein-swank "1.4.4"]]
  :java-source-path "src/java"
  :warn-on-reflection true)
