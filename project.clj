(defproject try-clj-http "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ["-Xmx128m"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.1"]
                 [org.clojure/java.jdbc "0.3.0"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [clj-time "0.7.0"]]
  :main try-clj-http.core)

