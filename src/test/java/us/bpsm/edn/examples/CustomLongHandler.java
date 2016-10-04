// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.examples;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.Test;

import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.parser.TagHandler;


public class CustomLongHandler {
    @Test
    public void test() throws IOException {
        Parser.Config cfg =
            Parsers.newParserConfigBuilder()
                .putTagHandler(Parser.Config.LONG_TAG, new TagHandler() {
                    @Override
					public Object transform(Tag tag, Object value) {
                        long n = (Long) value;
                        if (Integer.MIN_VALUE <= n && n <= Integer.MAX_VALUE) {
                            return Integer.valueOf((int) n);
                        } else {
                            return BigInteger.valueOf(n);
                        }
                    }
                }).build();
        Parser p = Parsers.newParser(cfg);
        Parseable pbr = Parsers.newParseable("1024, 2147483648");
        assertEquals(1024, p.nextValue(pbr));
        assertEquals(BigInteger.valueOf(2147483648L), p.nextValue(pbr));
    }
}
