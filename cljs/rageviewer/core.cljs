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
  (set! (.title utils/document) (str (aget rage "title") " | Rage Viewer"))
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
  (set! current-rage-index -1)
  (show-next-rage)
  (utils/show (utils/by-id "nav_bar"))
  (utils/hide (utils/by-id "nav_bar2")))

(defn ^:export load-rage [rage]
  (set! current-rage rage)
  (show-rage rage)
  (utils/show (utils/by-id "nav_bar2"))
  (utils/hide (utils/by-id "nav_bar")))    

(defn ^:export view-feedback []
  (utils/send-xhr
    "./viewed"
    "POST"
    (str "id=" (aget current-rage "id"))
    {"Content-Type" "application/x-www-form-urlencoded"}))

(defn ^:export open-rages [channel]
  (utils/open-jsonp (str "./rages/" channel) nil rageviewer.core.load_rages))

(defn ^:export init []
  (let [urlhash (js* "window.location.hash")]
    (if (<= (count urlhash) 1)
      (open-rages "f7u12")
      (utils/open-jsonp 
        (str "./rage/" (.substring urlhash 1)) nil 
          rageviewer.core.load_rage))))

(defn ^:export show-all []
  (set! (.hash (js* "window.location")) "") ;; remove hash
  (utils/open-jsonp "./rages" nil rageviewer.core.load_rages))

(defn make-rage-ele [rage]
  (utils/new-ele "li" 
    {"className" "top-rage-item" "innerHTML" (aget rage "title")}))

(defn ^:export load_top_rages [rages]
  (let [container (utils/new-ele "ul" {"id" "top-rages-list"})]
    (reduce 
      (fn [container rage]
        (do
          (utils/append-ele container (make-rage-ele rage))
          container))
      container rages)))

(defn ^:export show-top-rages []
  (if (nil? utils/by-id "top-rages-list")
    (utils/open-jsonp "./top" nil rageviewer.core.load_top_rages))
  (utils/toggle (utils/by-id "top-rages-container")))

(defn ^:export show-channel-selector []
  (utils/hide (utils/by-id "channel-label"))
  (utils/show (utils/by-id "channel-selector")))

(defn ^:export set-channel []
  (let [selector (utils/by-id "channel-selector")
        selected-index (.selectedIndex selector)
        selected-option (aget (.options selector) selected-index)
        selected-channel (.value selected-option)]
        
    (set! (.innerHTML (utils/by-id "channel-label")) selected-channel)
    (utils/show (utils/by-id "channel-label"))
    (utils/hide (utils/by-id "channel-selector"))
    (open-rages selected-channel)))

