(defproject backend "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 ; Compojure - A basic routing library
                 [compojure "1.6.2"]
                 ; Our Http library for client/server
                 [http-kit "2.5.0"]
                 ; Ring defaults - for query params etc
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring-cors "0.1.13"]
                 ;; Time lib
                 [clj-time "0.15.2"]
                 ; Clojure data.JSON library
                 [org.clojure/data.json "0.2.7"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]]
  :main ^:skip-aot backend.core
  :target-path "target/%s"
  :plugins [[lein-auto "0.1.3"]]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
