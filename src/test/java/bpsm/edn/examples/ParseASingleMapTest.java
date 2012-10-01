package bpsm.edn.examples;

import static bpsm.edn.model.Keyword.newKeyword;
import static bpsm.edn.model.Symbol.newSymbol;
import static bpsm.edn.parser.ParserConfiguration.defaultConfiguration;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Token;

public class ParseASingleMapTest {
    @Test
    public void simpleUsageExample() throws IOException {
        Parser p = Parser.newParser(defaultConfiguration(), "{:x 1, :y 2}");
        try {
            Map<?, ?> m = (Map<?, ?>) p.nextValue();
            assertEquals(m.get(newKeyword(newSymbol(null, "x"))), 1L);
            assertEquals(m.get(newKeyword(newSymbol(null, "y"))), 2L);

            assertEquals(Token.END_OF_INPUT, p.nextValue());
        } finally {
            p.close();
        }
    }
}
