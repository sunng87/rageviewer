(ns rageviewer.utils
  (:require [goog.net.XhrIo :as gxhr]
            [goog.net.Jsonp :as gjsonp]
            [goog.dom :as dom]))

(defn by-id [id] (dom/$ id))
(def document (dom/getDocument))

;; copied from mjg123's gist: https://gist.github.com/1098417
(defn as-js-obj [cljmap]
  (let [out (js-obj)]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn new-ele [name tags]
  (dom/createDom name (as-js-obj tags)))
(defn append-ele [parent child]
  (dom/appendChild parent child))

(defn show [ele]
  (set! (.display (aget ele "style")) "block"))
(defn hide [ele]
  (set! (.display (aget ele "style")) "none"))
(defn toggle [ele]  
  (if (= (.display (aget ele "style")) "none")
    (show ele)
    (hide ele)))
(def alert (js* "alert"))
(defn js-date [arg]
  (new (js* "Date") arg))
 
(defn timestamp-to-date [ts]
  (. (js-date (* 1000 ts)) (toUTCString)))
    
(defn open-jsonp [url, payload, callback]
  (let [jsonp (goog.net.Jsonp. url)]
    (.send jsonp
      payload
      callback)))

(defn send-xhr [url method content headers]
  (let [xhr (goog.net.XhrIo.)
        header-obj (as-js-obj headers)]
     (.send xhr url method content header-obj)))

