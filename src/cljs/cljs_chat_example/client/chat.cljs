(ns cljs-chat-example.client.chat
  (:use [jayq.core :only [$]]
        [jayq.util :only [clj->js]])
  (:require [crate.core :as crate]))

;===============================================================================
; cljs utils
;===============================================================================

(defn log [m]
  (.log js/console m))

(defn toJSON [o]
  (let [o (if (map? o) (clj->js o) o)]
    (.stringify (.-JSON js/window) o)))

(defn parseJSON [x]
  (.parse (.-JSON js/window) x))

;===============================================================================

(def websocket* (atom nil))

(defn- send-chat-msg [m]
  (.send @websocket* (toJSON {:m m}))
  (.focus ($ "#chatinput")))

(defn- receive-chat-msg [m]
  (.append ($ "#chats")
           (crate/html [:p m]))
  (.focus ($ "#chatinput")))

(defn- main []
  (log "main")
  (log "establishing websocket...")
  (reset! websocket* (js/WebSocket. "ws://localhost:8081/websocket"))
  (doall
    (map #(aset @websocket* (first %) (second %))
         [["onopen" (fn [] (log "OPEN"))]
          ["onclose" (fn [] (log "CLOSE"))]
          ["onerror" (fn [e] (log (str "ERROR:" e)))]
          ["onmessage" (fn [m]
                         (let [data (.-data m)
                               d (parseJSON data)
                               m (.-m d)]
                           (receive-chat-msg m)))]]))
  (.unload ($ js/window)
           (fn []
             (.close @websocket*)
             (reset! @websocket* nil)))
  (log "websocket loaded.")
  (.html ($ "#main")
         (crate/html
           [:div
            [:div {:id "chats"}]
            [:input {:style "display: block; border: 2px solid #66cc33;"
                     :id "chatinput"
                     :type "text"}]]))
  (.keydown ($ "#chatinput")
            (fn [event]
              (let [k (.-keyCode event)]
                (when (= k 13)
                  (send-chat-msg (.val ($ "#chatinput")))
                  (-> ($ "#chatinput")
                    (.val "")
                    (.focus))))))
  (.focus ($ "#chatinput")))

(.ready ($ js/document)
        (fn []
          (log "ready...")
          (main)))

