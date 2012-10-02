// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

class CharSequenceReader extends Reader {
    CharSequence cs;
    int leftMostUnread = 0;

    CharSequenceReader(CharSequence cs) {
        this.cs = cs;
    }

    static Reader newCharSequenceReader(CharSequence cs) {
        if (cs instanceof String) {
            return new StringReader((String)cs);
        } else {
            return new CharSequenceReader(cs);
        }
    }

    @Override
    public int read() throws IOException {
        if (cs == null) {
            throw new IOException("Can't read form a closed Reader");
        }
        if (leftMostUnread >= cs.length()) {
            return -1;
        }
        return cs.charAt(leftMostUnread++);
    }
    
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (cs == null) {
            throw new IOException("Can't read form a closed Reader");
        }
        int charsLeft = cs.length() - leftMostUnread;
        int charsToRead = Math.min(charsLeft, len);
        for (int i = 0; i < charsToRead; i++) {
            cbuf[off+i] = cs.charAt(leftMostUnread+i);
        }
        leftMostUnread = leftMostUnread + charsToRead;
        return charsToRead;
    }

    @Override
    public void close() throws IOException {
        cs = null;
    }
    
}
