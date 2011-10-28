(ns rageviewer.routes
  (:use compojure.core)
  (:use [rageviewer actions services helper])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :as ring]
            [clojure.tools.logging :as logging]))


(defn app-init []
  (do
    (init-db)
    (logging/info "database initialized")
    (refresh-rages)))

(defroutes default-routes
  (GET "/" [] (ring/redirect "index.html"))
  (GET "/rage/:id" [] get-rage)
  (GET "/rages/:channel" [] get-rages)
  (POST "/viewed" [] viewed)
  (GET "/top" [] get-top)
  (POST "/login" [] login)
  (GET "/logout" [] logout)
  (GET "/login-status" [] login-status)
  (POST "/upvote" [] upvote)
  (POST "/downvote" [] downvote)
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site default-routes))

