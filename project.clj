(defproject dbl-server-count "0.1.0-SNAPSHOT"
  :description "Updates Discord bot server count on Discord Bots List."
  :url "https://github.com/guacamoledragon/dbl-server-count"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main ^:skip-aot dbl-server-count.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
