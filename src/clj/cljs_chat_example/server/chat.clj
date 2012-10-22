(ns cljs-chat-example.server.chat
  (:use [ring.middleware reload params resource file-info]
        [ring.adapter.jetty :only [run-jetty]]
        [hiccup.page :only [include-js include-css html5]]
        [hiccup.element :only [javascript-tag]])
  (:require [clj-json.core :as json]
            [clojure.string :as string])
  (:import (org.webbitserver WebServer
                             WebServers
                             WebSocketHandler)
           (org.webbitserver.handler StaticFileHandler)))

;===============================================================================
; Handling Ring Requests
;===============================================================================

(defn render-notfound [req]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body (str "path not found: " (:uri req))})

(defmacro render-html5 [& elts]
  `{:status 200
    :headers {"Content-Type" "text/html; charset=UTF-8"}
    :body (html5 ~@elts)})

(defn- render-index [req]
  (render-html5
    [:head
     [:title "clojurescript chat example"]
     (include-css "/css")]
    [:body
     [:div#main "Loading..."]
     (include-js "//cdnjs.cloudflare.com/ajax/libs/jquery/1.8.2/jquery.js"
                 "//cdnjs.cloudflare.com/ajax/libs/json2/20110223/json2.js"
                 "/js/gen/dev/goog/base.js"
                 "/js/gen/dev/main.js")
     (javascript-tag "goog.require('cljs_chat_example.client.chat')")]))

(defn- render-css [req]
  {:status 200
   :headers {"Content-Type" "text/css"}
   :body "body {padding: 0; margin: 0;}
          #main {margin: 2em;}
          p {padding:0; margin:0; border: 1px solid #ddd;}
          input {display: block; width: 100%;}"})

(defn- ring-handler [req]
  (let [hf (case (req :uri)
             "/" render-index
             "/css" render-css
             render-notfound)]
    (hf req)))

(def ^:private ring-app*
  (-> #'ring-handler
    (wrap-resource "web")
    (wrap-file-info)
    (wrap-params)
    (wrap-reload '(iteractive.chat))))

;===============================================================================
; Webbit Handler / WebSockets
;===============================================================================

(def ^:private ws-conns* (ref #{}))

(defn- ws-stats [] (println "websocket count:" (count @ws-conns*)))

(defn- ws-open [c]
  (dosync (alter ws-conns* conj c))
  (ws-stats))

(defn- ws-close [c]
  (dosync (alter ws-conns* disj c))
  (ws-stats))

(defn- ws-message [c m]
  (println "websocket message:" m)
  (let [mj (json/parse-string m)
        msg (mj "m")]
    (doall
      (for [c @ws-conns*]
        (.send c (json/generate-string {"m" msg}))))))

(def ^:private ws-handler*
  (proxy [WebSocketHandler] []
    (onOpen [c] (ws-open c))
    (onClose [c] (ws-close c))
    (onMessage [c m] (ws-message c m))))

;===============================================================================
; Main
;===============================================================================

(defn -main []
  (println "starting jetty server...")
  (run-jetty #'ring-app* {:port 8080 :join? false})
  (println "starting webbit server...")
  (doto (WebServers/createWebServer 8081)
    (.add "/websocket" ws-handler*)
    (.start)))

