# todomvc-db-view

An example app that demonstrates the [db-view
approach](https://maxweber.github.io/blog/2019-07-25-introducing-db-view-part-1)
and implements a frontend and a backend for the well-known
[TodoMVC](http://todomvc.com/) example, an app to manage todo items.

## Install

Run once:

``` shell
yarn
```

to install all npm dependencies via [yarn](https://yarnpkg.com/).

## Development

To start the [shadow-cljs](http://shadow-cljs.org/) that builds the
client run:

``` shell
yarn watch
```

To start the Clojure server run:

``` shell
lein repl
```

to start a Clojure REPL via [Leiningen](https://leiningen.org/). The
initial namespace will automatically start the HTTP server and other
system components.

## License

Copyright © 2019 Max Weber

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
