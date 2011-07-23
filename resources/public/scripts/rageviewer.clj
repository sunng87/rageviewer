(ns rageviewer)

(def loaded-rages '())
(def current-rage-index -1)
(defn by-id (js* "document.getElementById"))

(defn load-rages [rages]
  (set! loaded-rages rages)
  ())

(defn show-next-rage []
  (if (< current-rage-index (count loaded-rages))
    (set! current-rage-index (inc current-rage-index))
    (show-rage current-rage-index)))

(defn show-prev-rage []
  (if (> currrent-rage-index 0)
    (set! current-rage-index (dec current-rage-index))
    (show-rage current-rage-index)))

(defn show-rage [rage]
  (set! (.innerHTML (by-id "rage-title")) (:title rage))
  (set! (.innerHTML (by-id "rage-voteup")) (:ups rage))
  (set! (.innerHTML (by-id "rage-votedown")) (:downs rage))
  (set! (.src (by-id "rage-img") 
              (if (re-matches #"[.png|.jpg]$" (:url rage))
                (:url rage)
                (str (:url rage) ".png")))))

