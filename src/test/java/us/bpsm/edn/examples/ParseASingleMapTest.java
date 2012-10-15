// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import static us.bpsm.edn.Keyword.newKeyword;
import static us.bpsm.edn.parser.Parsers.defaultConfiguration;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;


public class ParseASingleMapTest {
    @Test
    public void simpleUsageExample() throws IOException {
        Parseable pbr = Parsers.newParseable("{:x 1, :y 2}");
        Parser p = Parsers.newParser(defaultConfiguration());
        Map<?, ?> m = (Map<?, ?>) p.nextValue(pbr);
        assertEquals(m.get(newKeyword("x")), 1L);
        assertEquals(m.get(newKeyword("y")), 2L);
        assertEquals(Parser.END_OF_INPUT, p.nextValue(pbr));
    }
}
