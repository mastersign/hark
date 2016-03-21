(defproject hark "0.2.0-SNAPSHOT"
  :description "A Java / Clojure library, providing an OutputStream for parsing separated strings."
  :url "http://github.com/mastersign/hark"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :java-source-paths ["src/java"]
  :profiles {:dev {:dependencies [[junit/junit "4.11"]]
                   :java-source-paths ["src/java" "test/java"]}}
  :plugins [[lein-junit "1.1.8"]
            [lein-codox "0.9.4"]]
  :junit ["test/java"]
  :codox {:output-path "doc/clj"
          :source-uri "https://github.com/mastersign/hark/blob/{version}/{filepath}#{line}"})
