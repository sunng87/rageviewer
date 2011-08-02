(ns rageviewer)

(def loaded-rages)
(def current-rage-index -1)
(def current-rage nil)
(defn by-id [id]
  (.getElementById (js* "document") id))
(defn get-head []
  (or (aget (js* "document") "head")
      (js* "document.getElementsByTagName('head')[0]")))
(defn new-ele [name tags]
  (let [ele (.createElement (js* "document") name)]
    (do
      (doseq [key (keys tags)]
        (.setAttribute ele key (get tags key)))
      ele)))
(defn show [ele]
  (set! (.display (aget ele "style")) "block"))
(defn hide [ele]
  (set! (.display (aget ele "style")) "none"))
(defn js-date [arg]
  (new (js* "Date") arg))
 
(defn timestamp-to-date [ts]
  (. (js-date (* 1000 ts)) (toUTCString)))
    
(defn to-imgur-url [url]
  (or 
    (first (re-matches #"http://.*?(png|jpg)$" url))
    (str url ".png")))

(defn open-jsonp [url]
  (let [ele (new-ele "script" {"src" url "id" "jsonp-io"})]
    (.appendChild (get-head) ele)))

(defn close-jsonp []
  (let [ele (by-id "jsonp-io")]
    (.removeChild (get-head) ele)))

(defn show-rage [rage]
  (set! (.src (by-id "rage-img")) "")
  (set! (.alt (by-id "rage-img")) "loading...") 
  (set! (.innerHTML (by-id "rage-title")) (aget rage "title"))
  (set! (.innerHTML (by-id "rage-voteup")) (aget rage "ups"))
  (set! (.innerHTML (by-id "rage-votedown")) (aget rage "downs"))
  (set! (.title (by-id "rage-img")) (aget rage "title"))
  (set! (.innerHTML (by-id "rage-author")) (aget rage "author"))
  (set! (.href (by-id "rage-link")) 
    (str "http://www.reddit.com" (aget rage "permalink")))
  (set! (.innerHTML (by-id "rage-date")) 
    (timestamp-to-date (aget rage "created")))
  (set! (.src (by-id "rage-img")) 
    (to-imgur-url (aget rage "url"))))

(defn ^:export show-next-rage []
  (if (< current-rage-index (count loaded-rages))
    (do
      (set! current-rage-index (inc current-rage-index))
      (set! current-rage (aget loaded-rages current-rage-index))
      (show-rage current-rage))))

(defn ^:export show-prev-rage []
  (if (> current-rage-index 0)
    (do
      (set! current-rage-index (dec current-rage-index))
      (set! current-rage (aget loaded-rages current-rage-index))
      (show-rage current-rage))))

(defn ^:export load-rages [rages]
  (set! loaded-rages rages)
  (show-next-rage)
  (close-jsonp)
  (show (by-id "nav_bar"))
  (hide (by-id "nav_bar2")))

(defn ^:export load-rage [rage]
  (set! current-rage rage)
  (show-rage rage)
  (close-jsonp)
  (show (by-id "nav_bar2"))
  (hide (by-id "nav_bar")))    

(defn ^:export view-feedback []
  (let [xhr (new (js* "XMLHttpRequest") ())]
    (doto xhr
      (.open "POST" "./viewed")
      (.setRequestHeader "Content-Type" "application/x-www-form-urlencoded")
      (.send 
        (str "id=" 
          (aget current-rage "id"))))))

(defn ^:export init []
  (let [urlhash (js* "window.location.hash")]
    (if (<= (count urlhash) 1)
      (open-jsonp "./rages?callback=rageviewer.load_rages")
      (open-jsonp 
        (str "./rage/" (.substring urlhash 1) "?callback=rageviewer.load_rage")))))

