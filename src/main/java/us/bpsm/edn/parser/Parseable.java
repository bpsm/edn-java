package us.bpsm.edn.parser;

import java.io.Closeable;
import java.io.IOException;

/**
 * An edn {@link Parser} parses text from Objects implementing this
 * interface. The class {@link Parsers} provides facotry methods for
 * this type.
 */
public interface Parseable extends Closeable {

    /**
     * The value returned by {@link #read()} to indicate the end of
     * input available through this Parseable.
     */
    public static final int END_OF_INPUT = -1;

    /**
     * Read and return the next character from this Parseable as a
     * non-negative integer, or {@link #END_OF_INPUT}.
     */
    public int read() throws IOException;

    /**
     * Unread {@code ch}, such that the next call to {@code read()}
     * will return {@code ch}.  Unread can be used to unread at most
     * one character of input.
     *
     * <p>The behavior of calling {@code unread} before the first call
     * to {@code read()} is undefined. The behavior of calling
     * {@code unread(ch)} with something other than {@code ch}
     * returned by the most recent call to {@code read()} is
     * undefined. The behavior of calling {@code unread(ch)} more than
     * once without an intervening call to {@code read()} is
     * undefined.
     */
    public void unread(int ch) throws IOException;
}
