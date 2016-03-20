package net.kiertscher.io.hark;

import java.nio.charset.Charset;

/**
 * This class represents a set of options to use with {@link StringParsingOutputStream}.
 *
 * @author Tobias Kiertscher
 */
public class StringParsingOptions {

    /**
     * An instance of {@link StringParsingOptions} with the default options.
     */
    public static final StringParsingOptions DEFAULT = new StringParsingOptions(
            Charset.defaultCharset(),
            System.lineSeparator(),
            32,
            "?"
    );

    private Charset charset;

    private String separator;

    private int bufferSize;

    private String decodeReplacement;

    /**
     * Initializes a new instance of {@link StringParsingOptions}:
     *
     * @param charset The charset for decoding a byte stream.
     * @param separator The character sequence to detected separated strings.
     * @param bufferSize The size of the byte buffer for decoding.
     * @param decodeReplacement The string which is used to replace
     *                          malformed or unmappable characters during decoding.
     */
    public StringParsingOptions(Charset charset, String separator, int bufferSize, String decodeReplacement) {
        this.charset = charset;
        this.separator = separator;
        this.bufferSize = bufferSize;
        this.decodeReplacement = decodeReplacement;
    }

    /**
     * Initializes a new instance of {@link StringParsingOptions}:
     *
     * @param charset The charset for decoding a byte stream.
     * @param separator The character sequence to detected separated strings.
     * @param bufferSize The size of the byte buffer for decoding.
     */
    public StringParsingOptions(Charset charset, String separator, int bufferSize) {
        this(charset, separator, bufferSize, DEFAULT.getDecodeReplacement());
    }

    /**
     * Initializes a new instance of {@link StringParsingOptions}:
     *
     * @param charset The charset for decoding a byte stream.
     * @param separator The character sequence to detected separated strings.
     */
    public StringParsingOptions(Charset charset, String separator) {
        this(charset, separator, DEFAULT.getBufferSize());
    }

    /**
     * Initializes a new instance of {@link StringParsingOptions}:
     *
     * @param charset The charset for decoding a byte stream.
     */
    public StringParsingOptions(Charset charset) {
        this(charset, DEFAULT.getSeparator());
    }

    /**
     * Gets the charset for decoding.
     *
     * @return The charset for decoding.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Gets the character sequence for detecting separated strings.
     *
     * @return The separator for string detection.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Gets the size of the byte buffer.
     *
     * @return The byte buffer size.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets a string which is used to replace
     * malformed or unmappable characters during decoding.
     *
     * @return The decode replacement string.
     */
    public String getDecodeReplacement() { return decodeReplacement; }
}
