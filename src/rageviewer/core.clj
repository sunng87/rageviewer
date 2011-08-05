(ns rageviewer.core
  (:use compojure.core)
  (:use rageviewer.helper)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clj-redis.client :as redis]
            [clojure.contrib.logging :as logging])
  (:require [reddit.clj.core :as reddit]))

(def reddit-client (reddit/login nil nil))

(def rages (ref '()))
(declare *db*)

(defn save-rages [lrages]
  (doseq [rage-item lrages]
    (do
      (let [{id :id title :title ups :ups 
             downs :downs url :url author :author 
             permalink :permalink created :created} rage-item]
        (redis/hmset *db* 
          (get-rage-item-key id) ;;hash-key rage-item::abcde
          {
           "id" id
           "title" title
           "ups" (str ups)
           "downs" (str downs)
           "url" url
           "author" author
           "permalink" permalink
           "created" (str created)  ;; key-values
          })))))

(defn refresh-rages []
  (do
    (dosync
      (let [new-rages (reddit/reddits reddit-client "fffffffuuuuuuuuuuuu")]
        (if-not (empty? new-rages)
          (ref-set rages 
            (filter #(re-find #"imgur\.com" (:domain %)) new-rages))))) ;;only accept comics from imgur.com
    (logging/info (str (count @rages) " rages loaded"))
    (save-rages @rages)
    (logging/info (str (count @rages) " rages saved"))
  ))

(defn app-init []
  (do
    (def *db* (init-redis-connections))
    (logging/info "database initialized")
    (refresh-rages)
    (schedule-refresh-task refresh-rages)))

(defn update-view-count [id]
  (str (redis/zincrby *db* "rages-viewcount" 1 id)))

(defn get-rage-by-id [id]
  (let [rage-id (get-rage-item-key id)]
    (redis/hgetall *db* rage-id)))

(defroutes default-routes
  (GET "/" [] (redirect-to "index.html"))
  (GET "/rage/:id" [id callback]
    (let [rage (get-rage-by-id id)]
      (if-not (empty? rage)
        (json-response rage callback)
        {:status 404})))
  (GET "/rages" [callback]
    (json-response @rages callback))
  (POST "/viewed" [id]
    (update-view-count id))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site default-routes))

