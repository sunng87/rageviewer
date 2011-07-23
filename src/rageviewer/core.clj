(ns rageviewer.core
  (:use compojure.core)
  (:require [compojure.route :as route]
                [compojure.handler :as handler])
  (:require [reddit.clj.core :as reddit])
  (:require [clojure.contrib.json :as json])
  (:import [java.util.concurrent Executors TimeUnit]))

(def reddit-client (reddit/login nil nil))

(def rages (ref '()))

(defn refresh-rages []
  (dosync
    (let [new-rages (reddit/reddits reddit-client "fffffffuuuuuuuuuuuu")]
      (if-not (empty? new-rages)
        (ref-set rages new-rages)))))

(defn schedule-refresh-task [] 
  (.scheduleWithFixedDelay (. Executors newScheduledThreadPool 1) 
    refresh-rages 0 10 (. TimeUnit MINUTES)))

(defn json-response [data, callback]
  {:headers {"Content-Type" (if (nil? callback) "application/json" "text/javascript")}
   :body (if 
           (nil? callback) 
           (json/json-str data) 
           (str callback "(" (json/json-str data) ");"))})

(defroutes default-routes
  (GET "/rages" {params :params} []
    (json-response @rages (:callback params)))
  (route/files "/" {:root "./resources/public"})
  (route/not-found "Page not found"))

(defn app-init []
  (schedule-refresh-task))

(def app
  (handler/site default-routes))

