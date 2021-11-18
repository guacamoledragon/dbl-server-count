# dbl-server-count
> Updates Discord bot server count on bot directories.

## Supported Directories

* [Discord Bots List](https://discordbots.org/)
* [top.gg](https://top.gg/)
* [Bots On Discord](https://bots.ondiscord.xyz) _Coming Soon..._

## Usage

    $ lein run config.edn

Sample `config.edn` file

```clojure
{:bot              {:token "..."
                    :id    "..."}
 :top-gg           {:token "..."}
 :discord-bots-gg  {:token "..."
                    :url   "https://discord.bots.gg/api/v1/"}}
```

## License

Copyright © 2021 Guacamole Dragon, LLC

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
