(ns rageviewer.core
  (:require [rageviewer.utils :as utils]))

(def loaded-rages)
(def current-rage-index -1)
(def current-rage nil)
(defn to-imgur-url [url]
  (or 
    (first (re-matches #"http://.*?(png|jpg)$" url))
    (str url ".png")))

(defn show-rage [rage]
  (set! (.src (utils/by-id "rage-img")) "")
  (set! (.alt (utils/by-id "rage-img")) "loading...") 
  (set! (.innerHTML (utils/by-id "rage-title")) (aget rage "title"))
  (set! (.innerHTML (utils/by-id "rage-voteup")) (aget rage "ups"))
  (set! (.innerHTML (utils/by-id "rage-votedown")) (aget rage "downs"))
  (set! (.title (utils/by-id "rage-img")) (aget rage "title"))
  (set! (.innerHTML (utils/by-id "rage-author")) (aget rage "author"))
  (set! (.href (utils/by-id "rage-link")) 
    (str "http://www.reddit.com" (aget rage "permalink")))
  (set! (.innerHTML (utils/by-id "rage-date")) 
    (utils/timestamp-to-date (aget rage "created")))
  (set! (.src (utils/by-id "rage-img")) 
    (to-imgur-url (aget rage "url")))
  (set! (.href (utils/by-id "rage-link2")) (str "#" (aget rage "id")))
  ((js* "scroll") 0 0))

(defn ^:export show-next-rage []
  (if (< current-rage-index (- (count loaded-rages) 1))
    (do
      (set! current-rage-index (inc current-rage-index))
      (set! current-rage (aget loaded-rages current-rage-index))
      (show-rage current-rage))
    (utils/alert "Sorry, but this is the last one.")))

(defn ^:export show-prev-rage []
  (if (> current-rage-index 0)
    (do
      (set! current-rage-index (dec current-rage-index))
      (set! current-rage (aget loaded-rages current-rage-index))
      (show-rage current-rage))
    (utils/alert "Sorry, but this is the first one.")))

(defn ^:export load-rages [rages]
  (set! loaded-rages rages)
  (show-next-rage)
  (utils/show (utils/by-id "nav_bar"))
  (utils/hide (utils/by-id "nav_bar2")))

(defn ^:export load-rage [rage]
  (set! current-rage rage)
  (show-rage rage)
  (utils/show (utils/by-id "nav_bar2"))
  (utils/hide (utils/by-id "nav_bar")))    

(defn ^:export view-feedback []
  (let [xhr (utils/get-xhr)]
    (doto xhr
      (.open "POST" "./viewed")
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.send 
        (str "id=" 
          (aget current-rage "id"))))))

(defn ^:export init []
  (let [urlhash (js* "window.location.hash")]
    (if (<= (count urlhash) 1)
      (utils/open-jsonp "./rages" nil rageviewer.core.load_rages)
      (utils/open-jsonp 
        (str "./rage/" (.substring urlhash 1)) nil 
          rageviewer.core.load_rage))))

(defn ^:export show-all []
  (set! (.hash (js* "window.location")) "") ;; remove hash
  (utils/open-jsonp "./rages" nil rageviewer.core.load_rages))

