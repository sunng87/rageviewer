(ns rageviewer.actions
  (:use [rageviewer helper services])
  (:require [reddit.clj.core :as reddit]))

(defn get-rage [req]
  (let [id (:id (:params req))
        callback (:callback (:params req))
        rage (get-rage-by-id id)]
    (if-not (empty? rage)
      (json-response rage callback)
      {:status 404})))

(defn get-rages [req]
  (let [channel (:channel (:params req))
        callback (:callback (:params req))
        channel-rages (get-rages-by-channel channel)]
    (if-not (empty? channel-rages)
      (json-response channel-rages callback)
      {:status 404})))

(defn viewed [req]
  (let [id (:id (:params req))]
    (update-view-count id)))

(defn get-top [req]
  (let [callback (:callback (:params req))]
    (json-response (get-top-rages) callback)))

(defn login [req]
  (let [user (:user (:params req))
        passwd (:passwd (:params req))
        reddit-user (reddit/login user passwd)
        credentials (:credential reddit-user)]
    (if-not (nil? credentials)
      {:session {:reddit-user reddit-user
                 :user user}
       :body (str "OK:" user)}
      {:body "FAILED"})))

(defn logout [req]
  {:session {:reddit-user nil}
   :body "OK"})

(defn login-status [req]
  (if-let [user (:user (:session req))]
    {:body (str "OK" ":" user)}
    {:body "FAILED"}))

(defn upvote [req]
  (let [reddit-user (:reddit-user (:session req))
        reddit-id (:reddit-id (:params req))]
    (if-not (nil? reddit-user)
      (do
        (reddit/vote-up reddit-user reddit-id)
        {:body "OK"})
      {:body "FAILED"})))

(defn downvote [req]
  (let [reddit-user (:reddit-user (:session req))
        reddit-id (:reddit-id (:params req))]
    (if-not (nil? reddit-user)
      (do
        (reddit/vote-down reddit-user reddit-id)
        {:body "OK"})
      {:body "FAILED"})))



