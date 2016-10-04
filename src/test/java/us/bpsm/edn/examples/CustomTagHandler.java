// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.examples;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import us.bpsm.edn.Tag;
import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.parser.TagHandler;


public class CustomTagHandler {
    @Test
    public void test() throws IOException, URISyntaxException {
        Parser.Config cfg =
            Parsers.newParserConfigBuilder()
            .putTagHandler(Tag.newTag("us.bpsm", "uri"),
                new TagHandler() {
                @Override
				public Object transform(Tag tag, Object value) {
                    return URI.create((String) value);
                }
            }).build();
        Parser p = Parsers.newParser(cfg);
        Parseable pbr = Parsers.newParseable(
            "#us.bpsm/uri \"http://example.com\"");
        assertEquals(new URI("http://example.com"), p.nextValue(pbr));
    }
}
