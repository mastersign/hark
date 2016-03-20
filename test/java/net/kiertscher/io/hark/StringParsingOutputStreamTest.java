package net.kiertscher.io.hark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for StringParsingOutputStream
 */
public class StringParsingOutputStreamTest extends TestCase {

    private int _stringCount;
    private Random _rand;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public StringParsingOutputStreamTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(StringParsingOutputStreamTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _stringCount = 0;
        _rand = new Random(1);
    }

    public void testShortFlush() throws IOException {
        final StringBuffer sb = new StringBuffer();
        final StringListener listener = new StringListener() {
            public void onString(String line) {
                sb.append(line);
                _stringCount++;
            }
        };
        final StringParsingOptions opts = new StringParsingOptions(
                Charset.forName("UTF-8"),
                StringParsingOptions.DEFAULT.getSeparator(),
                4 // a buffer size shorter than the input
        );
        final StringParsingOutputStream s = new StringParsingOutputStream(listener, opts);
        final Writer w = new OutputStreamWriter(s, "UTF-8");

        w.write("a");
        w.write("b");
        w.write("cdef");
        w.flush();

        assertEquals(1, _stringCount);
        assertEquals("abcdef", sb.toString());

        w.close();
    }

    public void testLongFlush() throws IOException {
        final StringBuffer sb = new StringBuffer();
        final StringListener listener = new StringListener() {
            public void onString(String line) {
                sb.append(line);
                _stringCount++;
            }
        };
        final StringParsingOptions opts = new StringParsingOptions(
                Charset.forName("UTF-8"),
                StringParsingOptions.DEFAULT.getSeparator(),
                4 // a buffer size greater than the input
        );
        final StringParsingOutputStream s = new StringParsingOutputStream(listener, opts);
        final Writer w = new OutputStreamWriter(s, "UTF-8");

        w.write("a");
        w.write("b");
        w.flush();

        assertEquals(1, _stringCount);
        assertEquals("ab", sb.toString());

        w.close();
    }

    public void testMultiByteChar() throws IOException {
        final StringBuffer sb = new StringBuffer();
        final StringListener listener = new StringListener() {
            public void onString(String line) {
                sb.append(line);
                _stringCount++;
            }
        };
        final StringParsingOptions opts = new StringParsingOptions(
                Charset.forName("UTF-8"),
                StringParsingOptions.DEFAULT.getSeparator(),
                5 // a buffer size of five to provoke misaligned multi-byte chars
        );
        final StringParsingOutputStream s = new StringParsingOutputStream(listener, opts);
        final Writer w = new OutputStreamWriter(s, "UTF-8");

        w.write("\u0398");
        w.write("\u02DF");
        w.write("\u05E9");
        w.write("\u2023");
        w.flush();

        assertEquals(1, _stringCount);
        assertEquals("\u0398\u02DF\u05E9\u2023", sb.toString());

        w.close();
    }

    public void testLinesFlush() throws IOException {
        final StringBuffer sb = new StringBuffer();
        final String separator = System.lineSeparator();
        final StringListener listener = new StringListener() {
            public void onString(String line) {
                sb.append(line);
                sb.append(separator);
                _stringCount++;
            }
        };
        final StringParsingOptions opts = new StringParsingOptions(
                Charset.forName("UTF-8"),
                separator,
                10
        );
        final StringParsingOutputStream s = new StringParsingOutputStream(listener, opts);
        final Writer w = new OutputStreamWriter(s, "UTF-8");

        final String str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String str2 = "abcdefghijklmnopqrstuvwxyz";
        final String str3 = "12345678901234567890123456";
        w.write(str1 + separator);
        w.write(str2 + separator);
        w.write(str3 + separator);
        w.flush();

        assertEquals(3, _stringCount);
        assertEquals(
                str1 + separator + str2 + separator + str3 + separator,
                sb.toString());

        w.close();
    }

    public void testStreamLines() throws IOException {
        final StringBuffer sbExpected = new StringBuffer();
        final StringBuffer sbActual = new StringBuffer();
        final String separator = System.lineSeparator();
        final StringListener listener = new StringListener() {
            public void onString(String line) {
                sbActual.append(line);
                sbActual.append(separator);
                _stringCount++;
            }
        };
        final StringParsingOptions opts = new StringParsingOptions(
                Charset.forName("UTF-8"),
                separator,
                80
        );
        final StringParsingOutputStream s = new StringParsingOutputStream(listener, opts);
        final OutputStreamWriter w = new OutputStreamWriter(s, "UTF-8");

        final int lines = 10000;
        for (int i = 0; i < lines; i++) {
            String l = randomLine();
            w.write(l + separator);
            sbExpected.append(l + separator);
        }

        assertTrue(_stringCount > 0);

        w.flush();

        assertEquals(lines, _stringCount);
        assertEquals(sbExpected.toString(), sbActual.toString());

        w.close();
    }

    private String randomLine() {
        StringBuffer sb = new StringBuffer();
        int length = _rand.nextInt(80);
        for (int i = 0; i < length; i++) {
            sb.append((char) (_rand.nextInt(10) > 0 ? 65 + _rand.nextInt(26) : 32));
        }
        return sb.toString();
    }

    public void testPassingThrough() throws IOException {
        final StringListener listener = new StringListener() {
            public void onString(String line) {
                // nothing
            }
        };
        final ByteArrayOutputStream bas = new ByteArrayOutputStream();
        final StringParsingOptions opts = new StringParsingOptions(
                Charset.forName("US-ASCII")
        );
        final StringParsingOutputStream s = new StringParsingOutputStream(listener, bas, opts);

        byte[] buffer = new byte[2048];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte)(48 + _rand.nextInt(74));
        }

        s.write(buffer);
        s.close();

        byte[] result = bas.toByteArray();

        for (int i = 0; i < buffer.length; i++) {
            assertEquals(buffer[i], result[i]);
        }
    }
}
