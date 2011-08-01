(ns rageviewer.core
  (:use compojure.core)
  (:use rageviewer.helper)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clj-redis.client :as redis])
  (:require [reddit.clj.core :as reddit]))

(def reddit-client (reddit/login nil nil))

(def rages (ref '()))
(declare *db*)

(defn save-rages [lrages]
  (doseq [rage-item lrages]
    (do
    (redis/hmset *db* 
      (get-rage-item-key (:name rage-item)) ;;hash-key rage-item::t3_abcde
      {
       "name" (:name rage-item)
       "title" (:title rage-item)
       "ups" (str (:ups rage-item))
       "downs" (str (:downs rage-item))
       "url" (:url rage-item)
       "author" (:author rage-item)
       "permalink" (:permalink rage-item)
       "created" (str (:created rage-item))  ;; key-values
      }))))

(defn refresh-rages []
  (do
    (dosync
      (let [new-rages (reddit/reddits reddit-client "fffffffuuuuuuuuuuuu")]
        (if-not (empty? new-rages)
          (ref-set rages new-rages))))
    (save-rages @rages)
  ))

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

