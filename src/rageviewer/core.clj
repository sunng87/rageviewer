(ns rageviewer.core
  (:use compojure.core)
  (:use rageviewer.helper)
  (:require [compojure.route :as route]
                [compojure.handler :as handler])
  (:require [reddit.clj.core :as reddit]))

(def reddit-client (reddit/login nil nil))

(def rages (ref '()))

(defn refresh-rages []
  (dosync
    (let [new-rages (reddit/reddits reddit-client "fffffffuuuuuuuuuuuu")]
      (if-not (empty? new-rages)
        (ref-set rages new-rages)))))

(defroutes default-routes
  (GET "/" [] (redirect-to "index.html"))
  (GET "/rages" {params :params} []
    (json-response @rages (:callback params)))
  (route/resources "/")
  (route/not-found "Page not found"))

(defn app-init []
  (schedule-refresh-task refresh-rages))

(def app
  (handler/site default-routes))

