(ns net.kiertscher.io.hark
  (:import [java.nio.charset Charset]
           [net.kiertscher.io.hark StringParsingOutputStream
                                   StringListener]))

(defn- parse-charset
  [charset]
  (if (string? charset)
    (Charset/forName charset)
    charset))

(defn- parse-opts
  [opts]
  {:charset (parse-charset (get opts :charset StringParsingOutputStream/DefaultCharset))
   :separator (get opts :separator StringParsingOutputStream/DefaultSeparator)
   :buffer-size (get opts :buffer-size StringParsingOutputStream/DefaultBufferSize)})

(defn tap
  ([f out opts]
   (let [listener (reify
                    StringListener
                    (onString [_ v] (f v)))
         opts' (parse-opts opts)
         s (StringParsingOutputStream.
              listener
              (:charset opts')
              (:separator opts')
              (:buffer-size opts'))]
     (.setOut s out)
     s))
  ([f out]
   (tap f out {}))
  ([f]
   (tap f nil {})))
