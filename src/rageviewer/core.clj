(ns rageviewer.core
  (:use compojure.core)
  (:use rageviewer.helper)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clj-redis.client :as redis])
  (:require [reddit.clj.core :as reddit]))

(def reddit-client (reddit/login nil nil))

(def rages (ref '()))

(defn refresh-rages []
  (dosync
    (let [new-rages (reddit/reddits reddit-client "fffffffuuuuuuuuuuuu")]
      (if-not (empty? new-rages)
        (ref-set rages new-rages)))))

(defn app-init []
  (do
    (schedule-refresh-task refresh-rages)
    (def *db* (init-redis-connections))))

(defn update-view-count [id]
  (str (redis/zincrby *db* "rages-viewcount" 1 id)))

(defroutes default-routes
  (GET "/" [] (redirect-to "index.html"))
  (GET "/rages" {params :params} []
    (json-response @rages (:callback params)))
  (POST "/viewed" {params :params} []
    (update-view-count (:id params)))
  (GET "/env" {}
    (get-env "VMC_REDIS"))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site default-routes))

