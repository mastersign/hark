(ns net.kiertscher.io.hark
  (:import [java.nio.charset Charset]
           [net.kiertscher.io.hark StringParsingOutputStream
                                   StringListener
                                   StringParsingOptions]))

(defn- parse-charset
  [charset]
  (if (string? charset)
    (Charset/forName charset)
    charset))

(defn- parse-opts
  [args]
  (let [opts (apply hash-map args)]
    (StringParsingOptions.
      (parse-charset (get opts :charset (.getCharset StringParsingOptions/DEFAULT)))
      (get opts :separator (.getSeparator StringParsingOptions/DEFAULT))
      (get opts :buffer-size (.getBufferSize StringParsingOptions/DEFAULT))
      (get opts :decode-replacement (.getDecodeReplacement StringParsingOptions/DEFAULT)))))

(defn tap-in
  [f out & args]
   (let [listener (reify
                    StringListener
                    (onString [_ v] (f v)))
         opts (parse-opts args)]
     (StringParsingOutputStream. listener out opts)))

(defn tap
  [f & args]
  (apply tap-in f nil args))
