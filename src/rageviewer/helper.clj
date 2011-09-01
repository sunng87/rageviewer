(ns rageviewer.helper
  (:require [clj-redis.client :as redis])
  (:require [clojure.contrib.json :as json])
  (:require [clojure.contrib.logging :as logging])
  (:import [java.util.concurrent Executors TimeUnit]))

(defn- wrap-task [task]
  (try task (catch Exception e (logging/warn "Exception caught on scheduled task" e))))

(defn schedule-refresh-task [task] 
  (.scheduleWithFixedDelay (. Executors newScheduledThreadPool 2) 
    (wrap-task task) 15 15 (. TimeUnit MINUTES)))
  
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

(defn get-env [env-name]
  (System/getenv env-name))
                                
(defn get-vcap-redis-password []
  (let [services (get-env "VCAP_SERVICES")
        services-dict (json/read-json services false)]
    (-> services-dict
      (get "redis-2.2")
      (first)
      (get "credentials")
      (get "password"))))

(defn get-redis-url []
  (if-let [url (get-env "VMC_REDIS")]
    (str "redis://" "user:" (get-vcap-redis-password) "@" url) ; for cloud foundry 
    "redis://127.0.0.1:6379")) ; for local test 
 
(defn init-redis-connections []
  (redis/init {:url (get-redis-url)}))

(defn get-rage-item-key [id]
  (str "rage-item::" id))