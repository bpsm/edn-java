package us.bpsm.edn.parser;

import java.io.Closeable;
import java.io.IOException;

public interface Parseable extends Closeable {
    int read() throws IOException;
    void unread(int ch) throws IOException;
}
