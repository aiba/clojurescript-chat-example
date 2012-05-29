(ns cljs-chat-example.ring-utils
  (:use (hiccup page core util element)
        (ring.middleware reload params resource file-info)))

;===============================================================================
; Web utils
;===============================================================================

(defn render-plaintext [body]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body body})

(defn render-html [body]
  {:status 200
   :headers {"Content-Type" "text/html; charset=UTF-8"}
   :body body})

(defn render-notfound [req]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body (str "path not found: " (:uri req))})

(defmacro render-html5 [& elts]
  `(render-html (hiccup.page/html5 ~@elts)))

(defn include-cljs [path]
  (list (javascript-tag "var CLOSURE_NO_DEPS = true;")
        (include-js path)))


