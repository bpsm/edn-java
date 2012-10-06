// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.PushbackReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import bpsm.edn.Symbol;
import bpsm.edn.Tag;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;
import bpsm.edn.parser.TagHandler;

public class CustomTagHandler {
    @Test
    public void test() throws IOException, URISyntaxException {
        Parser.Config cfg =
            Parsers.newParserConfigBuilder()
                .putTagHandler(Tag.newTag(Symbol.newSymbol("bpsm", "uri")),
                    new TagHandler() {
                        public Object transform(Tag tag, Object value) {
                            return URI.create((String) value);
                        }
                    }).build();
        Parser p = Parsers.newParser(cfg);
        PushbackReader pbr = Parsers.newPushbackReader(
                "#bpsm/uri \"http://example.com\"");
        assertEquals(new URI("http://example.com"), p.nextValue(pbr));
    }
}
