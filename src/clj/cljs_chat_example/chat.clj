(ns cljs-chat-example.chat
  (:use cljs-chat-example.ring-utils
        [ring.middleware reload params resource file-info]
        [ring.adapter.jetty :only [run-jetty]]
        [hiccup.page :only [include-js include-css]]
        [cssgen :only [css]])
  (:require [clj-json.core :as json]
            [clojure.string :as string])
  (:import (org.webbitserver WebServer
                             WebServers
                             WebSocketHandler)
           (org.webbitserver.handler StaticFileHandler)))

;===============================================================================
; Regular Ring Handlers
;===============================================================================

(defn- render-index [req]
  (render-html5
    [:head
     [:title "clojurescript chat example"]
     (include-css "/css")]
    [:body
     [:div#main "Loading..."]
     (include-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.js")
     (include-js "http://ajax.cdnjs.com/ajax/libs/json2/20110223/json2.js")
     (include-cljs "/js/main.js")]))

(defn- render-css [req]
  {:status 200
   :headers {"Content-Type" "text/css"}
   :body
   (css
     [:body :padding 0 :margin 0]
     [:#main :margin "2em"]
     [:p :padding 0
         :margin 0
         :border "1px solid #ddd"]
     [:input :display :block
             :width "100%"])})

(defn- ring-handler [req]
  (let [hf (case (req :uri)
             "/" render-index
             "/css" render-css
             render-notfound)]
    (hf req)))

(def ^:private ring-app*
  (-> #'ring-handler
    (wrap-resource "gen/dev")
    (wrap-file-info)
    (wrap-params)
    (wrap-reload '(iteractive.chat))))

;===============================================================================
; Webbit Handler
;===============================================================================

; set of websoket connections
(def ^:private websocket-conns* (ref #{}))

(defn- websocket-open [c]
  (dosync (alter websocket-conns* conj c))
  (println "websocket count:" (count @websocket-conns*)))

(defn- websocket-close [c]
  (println "connection closed.  removing from set...")
  (dosync (alter websocket-conns* disj c))
  (println "(count websocket-conns*) ==> " (count @websocket-conns*)))

(defn- websocket-message [c m]
  (println "websocket message:" m)
  (let [mj (json/parse-string m)]
    (let [msg (mj "m")]
      (println "broadcasting...")
      (doall
        (for [c @websocket-conns*]
          (do
            (println "sending to" c)
            (.send c
                   (json/generate-string
                     {"m" msg}))))))))

(def ^:private websocket-handler*
  (proxy [WebSocketHandler] []
    (onOpen [c] (websocket-open c))
    (onClose [c] (websocket-close c))
    (onMessage [c m] (websocket-message c m))))

;===============================================================================
; Main
;===============================================================================

(defn -main []
  (println "starting jetty server...")
  (run-jetty #'ring-app* {:port 8080 :join? false})
  (println "starting webbit server...")
  (doto (WebServers/createWebServer 8081)
    (.add "/websocket" websocket-handler*)
    (.start)))

