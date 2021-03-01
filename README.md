# dbl-server-count
> Updates Discord bot server count on [Discord Bots List](https://discordbots.org/).

## Usage

    $ lein run credentials.edn

Sample `credentials.edn` file

```clojure
{:bot              {:token "..."
                    :id    "..."}
 :discord-bot-list {:token "..."}
 :discord-bots-gg  {:token "..."
                    :url   "https://discord.bots.gg/api/v1/"}}
```

## License

Copyright © 2018 Guacamole Dragon, LLC

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
