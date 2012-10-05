package bpsm.edn.parser;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;

class Readers {

    /**
     * Return a PushbackReader (size 1) for the given Readable.
     * If r is in fact already a PushbackReader, just return r
     * directly.
     * @param r
     * @return
     */
    static PushbackReader pushbackReader(final Readable r) {
        if (r instanceof PushbackReader) {
            return (PushbackReader) r;
        }
        if (r instanceof Reader) {
            // default pushback of 1 character is all we need
            return new PushbackReader((Reader)r);
        }
        Reader rdr = new Reader() {
            @Override
            public void close() throws IOException {
                if (r instanceof Closeable) {
                    ((Closeable)r).close();
                }
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return r.read(CharBuffer.wrap(cbuf, off, len));
            }
        };
        return new PushbackReader(new BufferedReader(rdr));
    }

}
