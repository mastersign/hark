package net.kiertscher.io.hark;

/**
 * The interface for a listener to {@link StringParsingOutputStream}.
 *
 * @author Tobias Kiertscher
 */
public interface StringListener {

    /**
     * Is called for every detected string.
     * @param s The detected string.
     */
    void onString(String s);
}
