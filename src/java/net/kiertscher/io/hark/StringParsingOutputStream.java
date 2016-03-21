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

    private OutputStream _out;
    private StringParsingOptions _options;

    private StringListener _listener;
    private ByteBuffer _byteBuffer;
    private CharsetDecoder _decoder;
    private CharBuffer _charBuffer;
    private StringBuffer _stringBuffer;

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     * @param out      The output stream to pass all operations to.
     * @param options  The options controlling the decoding and parsing.
     */
    public StringParsingOutputStream(StringListener listener, OutputStream out, StringParsingOptions options) {
        if (listener == null) throw new IllegalArgumentException("The argument listener must not be null.");
        _out = out;
        _options = options != null ? options : StringParsingOptions.DEFAULT;
        _listener = listener;
        _decoder = _options.getCharset().newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .replaceWith(_options.getDecodeReplacement());
        _byteBuffer = ByteBuffer.allocate(_options.getBufferSize());
        _charBuffer = CharBuffer.allocate((int) Math.ceil(_byteBuffer.capacity() * _decoder.maxCharsPerByte()));
        _stringBuffer = new StringBuffer();
    }

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     * @param options  The options controlling the decoding and parsing.
     */
    public StringParsingOutputStream(StringListener listener, StringParsingOptions options) {
        this(listener, null, options);
    }

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     * @param out      The output stream to pass all operations to.
     */
    public StringParsingOutputStream(StringListener listener, OutputStream out) {
        this(listener, out, null);
    }

    /**
     * Initializes a new instance of the {@link StringParsingOutputStream}.
     *
     * @param listener The listener which is called for every separated string.
     */
    public StringParsingOutputStream(StringListener listener) {
        this(listener, null, null);
    }

    /**
     * @return The string listener.
     */
    public StringListener getListener() {
        return _listener;
    }

    /**
     * @return The output stream to pass all operations to or {@code null}.
     */
    public OutputStream getOut() { return _out; }

    /**
     * @return The options controlling the decoding and parsing.
     */
    public StringParsingOptions getOptions() {
        return _options;
    }

    /**
     * {@inheritDoc}
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
     */
    @Override
    public void write(byte[] b) throws IOException {
        if (_out != null) _out.write(b);
        super.write(b);
    }

    /**
     * {@inheritDoc}
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
        String separator = _options.getSeparator();
        int pos = _stringBuffer.indexOf(separator, 0);
        if (pos < 0) {
            _lastPos = _stringBuffer.length();
            return null;
        } else {
            String line = _stringBuffer.substring(0, pos);
            _stringBuffer.delete(0, pos + separator.length());
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
     */
    @Override
    public void flush() throws IOException {
        if (_out != null) _out.flush();
        flushBuffers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (_out != null) _out.close();
        flushBuffers();
    }
}
