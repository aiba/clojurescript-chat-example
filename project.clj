(defproject cljs-chat-example "1.0"
  :source-path "src/clj"
  :plugins [[lein-cljsbuild "0.2.0"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring "1.1.0"]
                 [hiccup "1.0.0"]
                 [clj-json "0.5.0"]
                 ; CLJS deps
                 [crate "0.2.0-alpha3"]
                 [jayq "0.1.0-alpha4"]
                 [org.mozilla/rhino "1.7R3"]
                 ; webbit / websocket
                 [org.webbitserver/webbit "0.4.7"]
                 ; css generation
                 [cssgen "0.3.0-alpha1"]]
  :cljsbuild {
    :builds { :dev {:source-path "src/cljs"
                    :notify-command ["growlnotify" "-m"]
                    :compiler {:output-to "resources/gen/dev/js/main.js"
                               :optimizations :whitespace
                               :pretty-print true}}}}
  :main cljs-chat-example.chat)

