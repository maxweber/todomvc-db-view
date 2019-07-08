(defproject todomvc-db-view "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [http-kit "2.3.0"]
                 [ring/ring-core "1.7.1"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [buddy/buddy-core "1.6.0"]]
  :source-paths ["src/clj"]
  :plugins [[cider/cider-nrepl "0.21.1"]]
  :profiles {:dev {:dependencies [[clj-http "3.10.0"]]
                   :repl-options {:init-ns dev.init}}})
