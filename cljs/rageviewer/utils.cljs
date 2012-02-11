(ns rageviewer.utils
  (:require [goog.net.XhrIo :as gxhr]
            [goog.net.Jsonp :as gjsonp]
            [goog.dom :as dom]
            [goog.dom.classes :as gclass]))

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

(defn set-css [ele name value]
  (aset (aget ele "style") name value))
(defn get-css [ele name]
  (aget (aget ele "style") name))
(defn show
  ([ele] (show ele "block"))
  ([ele display] (set-css ele "display" display)))
(defn hide [ele]
  (set-css ele "display" "none"))
(defn toggle [ele]  
  (if (= (. (aget ele "style") -display) "none")
    (show ele)
    (hide ele)))
(def alert (js* "alert"))
(def this (js* "this"))
(defn js-date [arg]
  (new (js* "Date") arg))
(def encodeURI (js* "encodeURI"))
(def encodeURIComponent (js* "encodeURIComponent"))
(defn run-later [func time]
  ((js* "setTimeout") func time))
 
(defn timestamp-to-date [ts]
  (. (js-date (* 1000 ts)) (toUTCString)))
    
(defn open-jsonp [url, payload, callback]
  (let [jsonp (goog.net.Jsonp. url)]
    (.send jsonp
      payload
      callback)))

(defn send-xhr
  ([url method content headers] (send-xhr url method content headers nil))
  ([url method content headers callback]
  (let [xhr (goog.net.XhrIo.)
        header-obj (when headers (as-js-obj headers))]
    (if-not (nil? callback)
      (.addEventListener xhr goog.net.EventType/COMPLETE callback))
    (.send xhr url method content header-obj))))

(defn swap-class [ele class1 class2]
  (gclass/toggle ele class1)
  (gclass/toggle ele class2))


