(defproject cljs-chat-example "1.0"
  :source-paths ["src/clj"]
  :plugins [[lein-cljsbuild "0.2.9"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring "1.1.6"]
                 [hiccup "1.0.1"]
                 [clj-json "0.5.1"]
                 ; CLJS deps
                 [crate "0.2.1"]
                 [jayq "0.1.0-alpha4"]
                 ; webbit / websocket
                 [org.webbitserver/webbit "0.4.14"]]
  :cljsbuild {
    :builds { :dev {:source-path "src/cljs"
                    :compiler {:output-to "resources/web/js/gen/dev/main.js"
                               :output-dir "resources/web/js/gen/dev"
                               :optimizations :none
                               :pretty-print true}}}}
  :main cljs-chat-example.server.chat)

