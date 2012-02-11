(defproject rageviewer "1.0.0-SNAPSHOT"
  :description "A rage comic viewer"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.logging "0.2.3"]
                 [compojure "1.0.1"]
                 [reddit.clj "0.3.3"]
                 [clj-redis "0.0.12"]]
  :dev-dependencies [[lein-ring "0.5.4"]
                     [lein-cljsbuild "0.0.12"]]
  :cljsbuild {
    :source-path "cljs"
    :compiler {
      :output-to "resources/public/scripts/rageviewer.js"
      :optimizations :advanced}}
  :ring {:handler rageviewer.routes/app
         :init  rageviewer.routes/app-init})
