// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser.input;

import java.io.IOException;
import java.io.Reader;

import bpsm.edn.parser.EdnException;

public class ReaderInput implements Input {
    Reader r;

    ReaderInput(Reader r) {
        this.r = r;
    }


    public char next() {
        if (r == null) {
            return 0;
        }
        try {
            int x = r.read();
            if (x < 0) {
                try {
                    r.close();
                } finally {
                    r = null;
                }
                return 0;
            } else {
                return (char)x;
            }
        } catch (IOException e) {
            throw new EdnException(e);
        }
    }

}
