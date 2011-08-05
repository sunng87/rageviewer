(ns rageviewer.utils
  (:require [goog.net.XhrIo :as gxhr]
            [goog.net.Jsonp :as gjsonp]))

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

;; copied from mjg123's gist: https://gist.github.com/1098417
(defn make-js-map [cljmap]
  (let [out (js-obj)]
    (doall (map #(aset out (name (first %)) (second %)) cljmap))
    out))

(defn send-xhr [url method content headers]
  (let [xhr (goog.net.XhrIo.)
        header-obj (make-js-map headers)]
     (.send xhr url method content header-obj)))

