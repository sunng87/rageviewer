(ns rageviewer)

(def loaded-rages)
(def current-rage-index -1)
(defn by-id [id]
  (.getElementById (js* "document") id))

(defn ^:export show-rage [rage-index]
  (let [rage (nth loaded-rages rage-index)]
    (set! (.innerHTML (by-id "rage-title")) (aget rage "title"))
    (set! (.innerHTML (by-id "rage-voteup")) (aget rage "ups"))
    (set! (.innerHTML (by-id "rage-votedown")) (aget rage "downs"))
    (set! (.src (by-id "rage-img")) 
              (or 
                (first (re-matches #"http://.*?(png|jpg)$" (aget rage "url")))
                (str (aget rage "url") ".png")))))

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
    ((js* "console.log") rages)
    (set! loaded-rages rages)
    (show-next-rage)))

