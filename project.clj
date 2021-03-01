(defproject dbl-server-count "0.1.0-SNAPSHOT"
  :description "Updates Discord bot server count on Discord Bots List."
  :url "https://github.com/guacamoledragon/dbl-server-count"
  :dependencies [[cheshire "5.10.0"]
                 [clj-http "3.12.0"]
                 [com.fasterxml.jackson.core/jackson-core "2.12.1"]
                 [com.github.DiscordBotList/DBL-Java-Library "19abf19859"]
                 [net.dv8tion/JDA "4.2.0_228"]
                 [org.clojure/clojure "1.10.2"]
                 [org.slf4j/slf4j-nop "1.7.30"]]
  :repositories [["jcenter" "https://jcenter.bintray.com"]
                 ["jitpack" "https://jitpack.io"]]
  :plugins [[lein-ancient "0.6.15"]]
  :main ^:skip-aot dbl-server-stats.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
