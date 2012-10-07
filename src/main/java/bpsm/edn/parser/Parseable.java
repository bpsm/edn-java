package bpsm.edn.parser;

import java.io.Closeable;
import java.io.IOException;

public interface Parseable extends Closeable {
    int read() throws IOException;
    void unread() throws IOException;
}
