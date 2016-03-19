(defproject hark "0.1.0-SNAPSHOT"
  :description "A Clojure library for parsing an OutputStream for separated strings."
  :url "http://github.com/mastersign/hark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:dependencies [[junit/junit "4.11"]]
                   :java-source-paths ["src/java" "test/java"]}}
  :plugins [[lein-junit "1.1.8"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :java-source-paths ["src/java"]
  :junit ["test/java"])
