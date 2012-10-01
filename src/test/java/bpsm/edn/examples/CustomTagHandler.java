package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.ParserConfiguration;
import bpsm.edn.parser.TagHandler;

public class CustomTagHandler {
    @Test
    public void test() throws IOException, URISyntaxException {
        ParserConfiguration cfg =
            ParserConfiguration
                .builder()
                .putTagHandler(Tag.newTag(Symbol.newSymbol("bpsm", "uri")),
                    new TagHandler() {
                        public Object transform(Tag tag, Object value) {
                            return URI.create((String) value);
                        }
                    }).build();
        Parser p = Parser.newParser(cfg, "#bpsm/uri \"http://example.com\"");
        assertEquals(new URI("http://example.com"), (URI) p.nextValue());
    }
}
