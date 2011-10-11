(defproject rageviewer "1.0.0-SNAPSHOT"
  :description "A rage comic viewer"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/data.json "0.1.1"]
                 [org.clojure/tools.logging "0.2.3"]
                 [compojure "0.6.5"]
                 [reddit.clj "0.3.0"]
                 [clj-redis "0.0.12"]]
  :dev-dependencies [[lein-ring "0.4.5"]
                                   [lein-control "0.1.1"]]
  :ring {:handler rageviewer.core/app
         :init  rageviewer.core/app-init})
