(ns net.kiertscher.io.hark
  (:import [java.nio.charset Charset]
           [net.kiertscher.io.hark StringParsingOutputStream
                                   StringListener
                                   StringParsingOptions]))

(defn- parse-charset
  "Turns the given value into an instance of java.nio.charset.Charset."
  [charset]
  (if (string? charset)
    (Charset/forName charset)
    charset))

(defn- parse-opts
  "Parses the arguments as a map with options.
   Ignores unknown keys and provides default values for missing keys.

   The following option keys are supported:
   :charset, :separator, :buffer-size, and :decode-replacement."
  [& args]
  (let [opts (apply hash-map args)]
    (StringParsingOptions.
      (parse-charset (get opts :charset (.getCharset StringParsingOptions/DEFAULT)))
      (get opts :separator (.getSeparator StringParsingOptions/DEFAULT))
      (get opts :buffer-size (.getBufferSize StringParsingOptions/DEFAULT))
      (get opts :decode-replacement (.getDecodeReplacement StringParsingOptions/DEFAULT)))))

(defn tap-in
  "Creates an java.io.OutputStream which decodes all written data
   as a char sequence. The char sequence is searched for separated strings.
   The function f is called for every separated string.

   All interactions with the returned stream are passed to out.

   Additional arguments are treated as an option map with the following keys:
   :charset
       A java.nio.charset.Charset or a string with the name of a charset
       used to decode the data into a character sequence.
       The default value is Charset.defaultCharset().
   :separator
       A string as the separator to detect breaks between separated
       strings in the decoded character sequence.
       The default value is System.lineSeparator().
   :buffer-size
       The size of the byte buffer, used to decode the written data
       into a character sequence.
       A large buffer can lead to better throughput but it can take longer
       to detect the next separated string.
       A shorter buffer can lead to low throughput but minimizes delays
       in detecting the next separated string.
       The default value is 32.
   :decode-replacement
       A string which is used to replace malformed or unmappable byte sequences
       in the decoded char sequence.
       The default value is '?'."
  [f out & args]
   (let [listener (reify
                    StringListener
                    (onString [_ v] (f v)))
         opts (apply parse-opts args)]
     (StringParsingOutputStream. listener out opts)))

(defn tap
  "Creates an java.io.OutputStream which decodes all written data
   as a char sequence. The char sequence is searched for separated strings.
   The function f is called for every separated string.

   Additional arguments are treated as an option map.
   For a detailed description of the option see the doc string of tap-in."
  [f & args]
  (apply tap-in f nil args))
