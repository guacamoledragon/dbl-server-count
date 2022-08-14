(ns dbl-server-stats.core
  (:gen-class)
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [clojure.edn :as edn])
  (:import
   (net.dv8tion.jda.api.events
    Event
    ReadyEvent)
   (net.dv8tion.jda.api.events.guild
    GuildJoinEvent
    GuildLeaveEvent)
   (net.dv8tion.jda.api.hooks
    ListenerAdapter)
   (net.dv8tion.jda.api.sharding
    DefaultShardManagerBuilder)
   (org.discordbots.api.client
    DiscordBotListAPI
    DiscordBotListAPI$Builder)))

(defn update-server
  "Sends a POST request with the new server count for the bot-id."
  ([config stats]
   (let [{:keys [url bot-id token]} config]
     (update-server url bot-id token stats)))
  ([api-url bot-id token stats]
   (let [body    (cheshire/generate-string stats)
         headers {"Authorization" token}]

     (-> (str api-url "bots/" bot-id "/stats")
         (client/post {:body         body
                       :headers      headers
                       :content-type :json
                       :accept       :json})))))

(defn update-server-stats
  [^DiscordBotListAPI tg-api stats]
  (.setStats tg-api ^Integer (:guildCount stats)))

(defn bot-stats
  "Create stats map from the JDA api object"
  [^Event event]
  (let [shards (.. event getJDA getShardManager getShards)]
    {:guildCount (reduce
                  +
                  (map #(.. % getGuildCache size) shards))}))

(defn listener-adapter
  [tg-api bots-gg]
  (proxy [ListenerAdapter] []
    (onReady
      [^ReadyEvent event]
      ()
      (let [shard         (.. event getJDA)
            shard-manager (.. shard getShardManager)
            shard-string  (.. shard getShardInfo getShardString)]
        ;; wait until all shards are connected
        (comment
          ;; still not working yet
          (print "\n" shard-string "Waiting")
          (flush)
          (while (not (every? #(= "CONNECTED" %)
                              (map #(.. % getStatus toString)
                                   (.getShards shard-manager))))
            (print ".")
            (Thread/sleep 1000)
            (flush)))
        ;; Update Stats
        (let [stats (bot-stats event)]
          (println shard-string "Ready:" (:guildCount stats))
          (update-server-stats tg-api stats)
          (update-server bots-gg stats))))

    (onGuildJoin
      [^GuildJoinEvent event]
      (let [stats                      (bot-stats event)
            {:keys [url bot-id token]} bots-gg]
        (println "✅" (-> event .getGuild .getName) " - joined:" (:guildCount stats))
        (update-server-stats tg-api stats)
        (update-server url bot-id token stats)))

    (onGuildLeave
      [^GuildLeaveEvent event]
      (let [stats                      (bot-stats event)
            {:keys [url bot-id token]} bots-gg]
        (println "❌" (-> event .getGuild .getName) " - left:" (:guildCount stats))
        (update-server-stats tg-api stats)
        (update-server url bot-id token stats)))))

(defn -main
  [config-file & args]
  (let [{:keys [bot top-gg discord-bots-gg]} (-> config-file slurp edn/read-string)
        tg-api (-> (DiscordBotListAPI$Builder.)
                   (.token (:token top-gg))
                   (.botId (:id bot))
                   .build)
        bots-gg (assoc discord-bots-gg
                       :bot-id (:id bot))]
    (-> (DefaultShardManagerBuilder/createLight (:token bot))
        (.addEventListeners (object-array [(listener-adapter tg-api bots-gg)]))
        .build)))

(comment
  (import '(net.dv8tion.jda.api JDA)
          '(net.dv8tion.jda.api.shajding DefaultShardManager))
  (def config (-> "credentials.edn" slurp edn/read-string))
  (def app (-main "credentials.edn"))
  (.shutdown ^DefaultShardManager app))
