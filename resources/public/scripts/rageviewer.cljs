(ns rageviewer)

(def loaded-rages)
(def current-rage-index -1)
(defn by-id [id]
  (.getElementById (js* "document") id))
(defn js-date [arg]
  (new (js* "Date") arg))
 
(defn timestamp-to-date [ts]
  (. (js-date (* 1000 ts)) (toUTCString)))
    
(defn to-imgur-url [url]
  (or 
    (first (re-matches #"http://.*?(png|jpg)$" url))
    (str url ".png")))
    
(defn show-rage [rage-index]
  (let [rage (nth loaded-rages rage-index)]
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
      (to-imgur-url (aget rage "url")))))

(defn ^:export show-next-rage []
  (if (< current-rage-index (count loaded-rages))
    (do
      (set! current-rage-index (inc current-rage-index))
      (show-rage current-rage-index))))

(defn ^:export show-prev-rage []
  (if (> current-rage-index 0)
    (do
      (set! current-rage-index (dec current-rage-index))
      (show-rage current-rage-index))))

(defn ^:export load-rages [rages]
  (do
    (set! loaded-rages rages)
    (show-next-rage)))

