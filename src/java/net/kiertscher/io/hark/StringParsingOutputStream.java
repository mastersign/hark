package net.kiertscher.io.hark;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * A sub class for {@link OutputStream} decoding the streamed bytes as a char sequence
 * and detecting separated strings, e.g. lines.
 * For every detected string a listener is called.
 * The charset for decoding the bytes to chars can be given to the constructor,
 * as well as the separator used to detect strings.
 *
 * @author Tobias Kiertscher
 */
public class StringParsingOutputStream extends OutputStream {

    /**
     * The default buffer size which is used if no buffer size is given to the constructor.
     * Is {@code 32}, if not changed.
     */
    public static int DefaultBufferSize = 32;

    /**
     * The default character set which is used if no character set is given to the constructor.
     * Is {@code System.defaultCharset()}, if not changed.
     */
    public static Charset DefaultCharset = Charset.defaultCharset();

    /**
     * The default separator which is used if no separator is given to the construtor.
     * Is {@code System.lineSeparator()}, if not changed.
     */
    public static String DefaultSeparator = System.lineSeparator();

    /**
     * The replacement string for malformed byte sequences.
     * Is a question mark, if not changed.
     */
    public static String DefaultReplacement = "?";

    private StringListener _listener;
    private ByteBuffer _byteBuffer;
    private Charset _charset;
    private CharsetDecoder _decoder;
    private CharBuffer _charBuffer;
    private StringBuffer _stringBuffer;
    private String _separator;

    private OutputStream _out;

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     * @param charset    The character set used to decode the stream.
     * @param separator  The separator used to detect line breaks.
     * @param bufferSize The size of the decoding buffer.
     */
    public StringParsingOutputStream(StringListener listener, Charset charset, String separator, int bufferSize) {
        if (listener == null) throw new IllegalArgumentException("The argument listener must not be null.");
        if (charset == null) throw new IllegalArgumentException("The argument charset must not be null.");
        if (separator == null) throw new IllegalArgumentException("The argument separator must not be null.");
        if (bufferSize < 4) throw new IllegalArgumentException("The buffer size must be at least 4.");
        _listener = listener;
        _separator = separator;
        _charset = charset;
        _decoder = _charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith(DefaultReplacement);
        _byteBuffer = ByteBuffer.allocate(bufferSize);
        _charBuffer = CharBuffer.allocate((int) Math.ceil(_byteBuffer.capacity() * _decoder.maxCharsPerByte()));
        _stringBuffer = new StringBuffer();
    }

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     * @param charset   The character set used to decode the stream.
     * @param separator The separator used to detect line breaks.
     */
    public StringParsingOutputStream(StringListener listener, Charset charset, String separator) {
        this(listener, charset, separator, DefaultBufferSize);
    }

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     * @param charset The character set used to decode the stream.
     */
    public StringParsingOutputStream(StringListener listener, Charset charset) {
        this(listener, charset, DefaultSeparator);
    }

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     */
    public StringParsingOutputStream(StringListener listener) {
        this(listener, DefaultCharset);
    }

    /**
     * @return The string listener.
     */
    public StringListener getListener() {
        return _listener;
    }

    /**
     * @return The character set which is used for decoding the stream.
     */
    public Charset getCharset() {
        return _charset;
    }

    /**
     * @return The string which is used to separate strings.
     */
    public String getSeparator() {
        return _separator;
    }

    /**
     * @return The output stream to pass all operations to or {@code null}.
     */
    public OutputStream getOut() { return _out; }

    /**
     * Sets the output stream.
     *
     * @param out A stream to pass all operations to or {@code null}.
     */
    public void setOut(OutputStream out) {
        _out = out;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        if (_out != null) _out.write(b);

        _byteBuffer.put((byte) b);
        if (!_byteBuffer.hasRemaining()) {
            decodeByteBuffer();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(byte[] b) throws IOException {
        if (_out != null) _out.write(b);
        super.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (_out != null) _out.write(b, off, len);
        super.write(b, off, len);
    }

    /**
     * Decodes the current content of the input byte buffer and
     * calls {@link StringListener#onString(String)} for every decoded line.
     */
    private void decodeByteBuffer() {
        _byteBuffer.rewind();
        CoderResult result = _decoder.decode(_byteBuffer, _charBuffer, false);
        _byteBuffer.compact();
        if (result.isError()) {
            // nothing, malformed and unmappable characters are replaced automatically
        }
        _stringBuffer.append(_charBuffer.array(), 0, _charBuffer.position());
        _charBuffer.rewind();
        String line;
        while ((line = processStringBuffer()) != null) {
            _listener.onString(line);
        }
    }

    /**
     * The last position up to which the string buffer was searched for the line separator.
     */
    private int _lastPos;

    /**
     * Searches for the line separator in the string buffer.
     * Returns all characters up to the first line separator, if one is found.
     * Removes all characters up to and including the first line separator from
     * the string buffer.
     * Returns null if no line separator was found.
     *
     * @return The next line from the string buffer, if a line separator was found; otherwise null.
     */
    private String processStringBuffer() {
        int pos = _stringBuffer.indexOf(_separator, 0);
        if (pos < 0) {
            _lastPos = _stringBuffer.length();
            return null;
        } else {
            String line = _stringBuffer.substring(0, pos);
            _stringBuffer.delete(0, pos + _separator.length());
            _lastPos = 0;
            return line;
        }
    }

    /**
     * Returns the remaining characters in the string buffer
     * or null if the string buffer is empty.
     * Clears the string buffer.
     *
     * @return The remaining string from the string buffer or null.
     */
    private String flushStringBuffer() {
        if (_stringBuffer.length() == 0) return null;
        String line = _stringBuffer.toString();
        _stringBuffer.delete(0, _stringBuffer.length());
        _lastPos = 0;
        return line;
    }

    /**
     * Decodes all remaining bytes in the byte buffer and flushes
     * the string buffer, calling {@link StringListener#onString(String)}
     * for every separated string and one time for the remaining characters, if there are any.
     */
    private void flushBuffers() {
        ByteBuffer used = ByteBuffer.wrap(_byteBuffer.array(), 0, _byteBuffer.position());
        _decoder.decode(used, _charBuffer, true);
        _byteBuffer.rewind();
        _decoder.flush(_charBuffer);
        _stringBuffer.append(_charBuffer.array(), 0, _charBuffer.position());
        _charBuffer.rewind();
        _decoder.reset();

        if (_stringBuffer.length() == 0) return;
        String line;
        while ((line = processStringBuffer()) != null) {
            _listener.onString(line);
        }
        line = flushStringBuffer();
        if (line != null) {
            _listener.onString(line);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
        if (_out != null) _out.flush();
        flushBuffers();
    }

    /**
     * {@inheritDoc}
     *
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        if (_out != null) _out.close();
        flushBuffers();
    }
}
