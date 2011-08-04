(ns rageviewer.utils
  (:require [goog.net.DefaultXmlHttpFactory :as gxhr]))

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
    
(defn open-jsonp [url]
  (let [ele (new-ele "script" {"src" url "id" "jsonp-io"})]
    (.appendChild (get-head) ele)))

(defn close-jsonp []
  (let [ele (by-id "jsonp-io")]
    (.removeChild (get-head) ele)))

(defn get-xhr []
  (. (new goog.net.DefaultXmlHttpFactory ()) (createInstance)))
