# hark

[![build status][travis-img]][travis-url]

> A Java / Clojure library, providing a `java.io.OutputStream` for parsing separated strings.

Inspired by ideas of Daniel Kiertscher.

## Usage

This library can be used as a Clojure library as well as a Java library.

### Clojure

Add `[hark "0.1.0-SNAPSHOT"]` to the dependencies in your `project.clj`.

```clojure
(require '(clojure.java.io :as io))
(require '(net.kiertscher.io.hark :as h))

(defn handler
  [s]
  (println (str "Detected line: " s)))

(with-open [s (h/tap handler :charset "UTF-8")]
  ;; write large amounts of encoded text to s
  ;; handler is called for every decoded line
  )
```

### Java

Download the [latest release] and put in on your class path.

```java
import java.nio.charset.Charset;
import net.kiertscher.io.hark.StringListener;
import net.kiertscher.io.hark.StringParsingOptions;
import net.kiertscher.io.hark.StringParsingOutputStream;

...

StringListener listener = new StringListener() {
    public void onString(String s) {
        System.out.println("Detected line: " + s);
    }
};

StringParsingOptions opts = new StringParsingOptions(
    Charset.forName("UTF-8")
);
StringParsingOutputStream s = new StringParsingOutputStream(listener, opts);

// write encoded text data to s
// listener.onString(s) is called for every decoded line

s.close();

...
```

## License

Copyright © 2016 Tobias Kiertscher <dev@mastersign.de>

Distributed under the Eclipse Public License either version 1.0 or
(at your option) any later version.

[travis-img]: https://img.shields.io/travis/mastersign/hark/master.svg
[travis-url]: https://travis-ci.org/mastersign/hark
[latest release]: https://github.com/mastersign/hark/releases/latest
