// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.examples;

import static bpsm.edn.Keyword.newKeyword;
import static bpsm.edn.Symbol.newSymbol;
import static bpsm.edn.parser.Parsers.defaultConfiguration;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.Map;
import org.junit.Test;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;

public class ParseASingleMapTest {
    @Test
    public void simpleUsageExample() throws IOException {
        PushbackReader pbr = Parsers.newPushbackReader("{:x 1, :y 2}");
        Parser p = Parsers.newParser(defaultConfiguration());
        Map<?, ?> m = (Map<?, ?>) p.nextValue(pbr);
        assertEquals(m.get(newKeyword(newSymbol(null, "x"))), 1L);
        assertEquals(m.get(newKeyword(newSymbol(null, "y"))), 2L);
        assertEquals(Parser.END_OF_INPUT, p.nextValue(pbr));
    }
}
