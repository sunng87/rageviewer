(ns rageviewer.helper
  (:require [clojure.contrib.json :as json])
  (:import [java.util.concurrent Executors TimeUnit]))

(defn schedule-refresh-task [task] 
  (.scheduleWithFixedDelay (. Executors newScheduledThreadPool 1) 
    task 0 10 (. TimeUnit MINUTES)))
  
(defn redirect-to [path]
  {:status 302
   :headers {"Location" path}
  })

(defn json-response [data, callback]
  {:headers 
    {"Content-Type" (if (nil? callback) 
        "application/json" "text/javascript")}
   :body (if 
           (nil? callback) 
           (json/json-str data) 
           (str callback "(" (json/json-str data) ");"))})    
