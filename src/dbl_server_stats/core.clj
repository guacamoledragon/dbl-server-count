(ns dbl-server-stats.core
  (:require
   [cheshire.core :as cheshire]
   [clj-http.client :as client]
   [clojure.edn :as edn])
  (:gen-class)
  (:import
   (net.dv8tion.jda.api.events Event ReadyEvent)
   (net.dv8tion.jda.api.events.guild GuildJoinEvent GuildLeaveEvent)
   (net.dv8tion.jda.api.hooks ListenerAdapter)
   (net.dv8tion.jda.api.sharding DefaultShardManagerBuilder)
   (org.discordbots.api.client DiscordBotListAPI$Builder DiscordBotListAPI)))

(defn update-server
  "Sends a POST request with the new server count for the bot-id."
  [api-url bot-id token stats]
  (let [body    (cheshire/generate-string stats)
        headers {"Authorization" token}]

    (-> (str api-url "bots/" bot-id "/stats")
        (client/post {:body         body
                      :headers      headers
                      :content-type :json
                      :accept       :json}))))

(defn update-server-stats
  [^DiscordBotListAPI dbl-api stats]
  (.setStats dbl-api ^Integer (:guildCount stats)))

(defn bot-stats
  "Create stats map from the JDA api object"
  [^Event event]
  (let [shards (.. event getJDA getShardManager getShards)]
    {:guildCount (reduce
                  +
                  (map #(.. % getGuildCache size) shards))}))

(defn listener-adapter [dbl-api bots-gg]
  (proxy [ListenerAdapter] []
    (onReady [^ReadyEvent event]
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
        (let [stats {:guildCount (count (.getGuilds shard-manager))}
              {:keys [url bot-id token]} bots-gg]

          (println shard-string "Ready:" (:guildCount stats))
          (update-server-stats dbl-api stats)
          (update-server url bot-id token stats))))

    (onGuildJoin [^GuildJoinEvent event]
      (let [stats                      (bot-stats event)
            {:keys [url bot-id token]} bots-gg]
        (println (-> event .getGuild .getName) " - joined:" (:guildCount stats))
        (update-server-stats dbl-api stats)
        (update-server url bot-id token stats)))

    (onGuildLeave [^GuildLeaveEvent event]
      (let [stats                      (bot-stats event)
            {:keys [url bot-id token]} bots-gg]
        (println (-> event .getGuild .getName) " - left:" (:guildCount stats))
        (update-server-stats dbl-api stats)
        (update-server url bot-id token stats)))))

(defn -main
  [config-file & args]
  (let [{:keys [bot discord-bot-list discord-bots-gg]} (-> config-file slurp edn/read-string)
        dbl-api (-> (DiscordBotListAPI$Builder.)
                    (.token (:token discord-bot-list))
                    (.botId (:id bot))
                    .build)
        bots-gg (assoc discord-bots-gg
                       :bot-id (:id bot))]
    (-> (DefaultShardManagerBuilder/createLight (:token bot))
        (.addEventListeners (object-array [(listener-adapter dbl-api bots-gg)]))
        .build)))

(comment
  (import '(net.dv8tion.jda.api JDA)
          '(net.dv8tion.jda.api.sharding DefaultShardManager))
  (def config (-> "credentials.edn" slurp edn/read-string))
  (def ^DefaultShardManager shard-manager (-> (get-in config [:bot :token])
                                              (DefaultShardManagerBuilder/createLight)
                                              ;;(.addEventListeners (object-array [(listener-adapter dbl-api bots-gg)]))
                                              .build))

  (.getShardsTotal shard-manager)
  (.getShardsQueued shard-manager)
  (while (not (every? #(= "CONNECTED" %)
                      (map #(.. % getStatus toString)
                           (.getShards shard-manager))))
    (print ".")
    (Thread/sleep 1000)
    (flush))
  (doseq [^JDA shard (.getShards shard-manager)]
    (println :status (.. shard getStatus toString))
    (println :shard-info (.. shard getShardInfo getShardString)))
  (.stop shard-manager)
  (count (.getGuilds shard-manager)))
