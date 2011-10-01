(ns rageviewer.core
  (:use compojure.core)
  (:use rageviewer.helper)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clj-redis.client :as redis]
            [clojure.contrib.logging :as logging])
  (:require [reddit.clj.core :as reddit]))

(def reddit-client (reddit/login nil nil))

(def rages (ref {}))
(def rage-subreddits
  {:f7u12 "fffffffuuuuuuuuuuuu"
   :novels "ragenovels"
   :recipes "fffffffuuuuuuuuuuuud"
   :classic "classicrage"
   :ladies "trollxchromosomes"
   :news "nnnnnnneeeeeeeeeeeews"
   :athiest "aaaaaatheismmmmmmmmmm"
   :military "rageops"
   :dreams "ragedreams"
   :historical "historicalrage"})
(defonce rages-viewcount "rages-viewcount")
(declare *db*)

(defn save-rages [lrages]
  (doseq [rage-item lrages]
    (do
      (let [{id :id title :title over_18 :over_18 ups :ups 
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
           "created" (str created)
           "over_18" (str over_18)  ;; key-values
          })))))

(defn- download-rage [name]
  (let [new-rages (reddit/reddits reddit-client (rage-subreddits name))]
    (if-not (empty? new-rages)
      (dosync
        (alter rages assoc name
          (filter #(re-find #"imgur\.com" (:domain %)) new-rages)))))) ;;only accept comics from imgur.com

(defn refresh-rages []
  (try
    (do
      (doall (pmap download-rage (keys rage-subreddits)))
      (let [loaded-rages (apply concat (vals @rages))]
        (logging/info (str (count loaded-rages) " rages loaded"))
        (save-rages loaded-rages)
        (logging/info (str (count loaded-rages) " rages saved"))))
    (catch Exception e (logging/warn "Exception caught when refresh rages" e))
    (finally
      (schedule refresh-rages 15))))

(defn app-init []
  (do
    (def *db* (init-redis-connections))
    (logging/info "database initialized")
    (refresh-rages)))

(defn update-view-count [id]
  (str (redis/zincrby *db* rages-viewcount 1 id)))

(defn get-rage-by-id [id]
  (let [rage-id (get-rage-item-key id)]
    (redis/hgetall *db* rage-id)))
          
(defn get-top-rages []
  (let [top-rages (redis/zrevrange *db* rages-viewcount 0 4)]
    (pmap get-rage-by-id top-rages)))

(defroutes default-routes
  (GET "/" [] (redirect-to "index.html"))
  (GET "/rage/:id" [id callback]
    (let [rage (get-rage-by-id id)]
      (if-not (empty? rage)
        (json-response rage callback)
        {:status 404})))
  (GET "/rages/:channel" [channel callback]
    (let [channel-rages (@rages (keyword channel))]
      (if-not (empty? channel-rages)
        (json-response channel-rages callback)
        {:status 404})))
  (POST "/viewed" [id]
    (update-view-count id))
  (GET "/top" [callback]
    (json-response (get-top-rages) callback))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site default-routes))

