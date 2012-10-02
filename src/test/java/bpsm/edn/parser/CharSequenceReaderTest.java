// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.parser.CharSequenceReader.newCharSequenceReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class CharSequenceReaderTest {

    @Test
    public void stringsAreReadByStringReader() {
        assertTrue(newCharSequenceReader("") instanceof StringReader);
        assertTrue(newCharSequenceReader(cs("")) instanceof CharSequenceReader);
        assertTrue(csr("") instanceof CharSequenceReader);
    }
    
    @Test
    public void test() throws IOException {
        assertEquals(-1, csr("").read());
        
        Reader r = csr("ab");
        assertEquals('a', r.read());
        assertEquals('b', r.read());
        assertEquals(-1, r.read());
        
        r = csr("abcdefg");
        char[] buf = new char[3];
        assertEquals(3, r.read(buf));
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals('d', r.read());
        assertEquals(2, r.read(buf, 1, 2));
        assertEquals('a', buf[0]);
        assertEquals('e', buf[1]);
        assertEquals('f', buf[2]);
        assertEquals(1, r.read(buf, 0, 3));
        assertEquals('g', buf[0]);
        assertEquals('e', buf[1]);
        assertEquals('f', buf[2]);
        assertEquals(0, r.read(buf));
        assertEquals('g', buf[0]);
        assertEquals('e', buf[1]);
        assertEquals('f', buf[2]);
        
        r = csr("");
        assertEquals(-1, r.read());
        assertEquals(-1, r.read());
    }
    
    @Test
    public void bordercases() {
        Reader r = csr("");
        try {
            r.close();
        } catch (IOException _) {
            fail();
        }
        try {
            r.read();
            fail();
        } catch (IOException _) {
            //ok
        }
    }
    
    static Reader csr(String s) {
        return newCharSequenceReader(cs(s));
    }
    
    static CharSequence cs(final String x) {
        return new CharSequence() {
            public CharSequence subSequence(int start, int end) {
                return x.subSequence(start, end);
            }
            public int length() {
                return x.length();
            }
            public char charAt(int index) {
                return x.charAt(index);
            }
        };
    }

}
