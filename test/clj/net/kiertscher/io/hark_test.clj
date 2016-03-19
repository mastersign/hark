(ns net.kiertscher.io.hark-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [net.kiertscher.io.hark :as h]))

(defn- count-output [n]
  (let [collected (atom [])
          handler-f (fn [l]
                      (swap! collected #(conj % l)))]
      (with-open [s (h/tap handler-f nil {:separator "X" :charset "UTF-8"})
                  w (io/writer s :encoding "UTF-8")]
        (binding [*out* w]
          (doseq [i (range n)]
            (print "ABCDEFG X"))))
      (count @collected)))

(deftest one-line
  (is (= 100 (count-output 100))))
