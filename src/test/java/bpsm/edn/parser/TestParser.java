package bpsm.edn.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.model.TaggedValue;
import bpsm.edn.parser.input.CharSequenceInput;

public class TestParser {

    @Test
    public void parseEdnSample() throws IOException {

        Parser parser = parser(IOUtil
                .stringFromResource("bpsm/edn/edn-sample.txt"));

        List<Object> expected = Arrays.asList(
                map(TestScanner.key("keyword"), TestScanner.sym("symbol"), 1,
                        2.0d, new BigInteger("3"), new BigDecimal("4.0")),
                Arrays.asList(1, 1, 2, 3, 5, 8),
                new HashSet<Object>(Arrays.asList('\n', '\t')),
                Arrays.asList(Arrays.asList(Arrays.asList(true, false, null))));

        List<Object> results = new ArrayList<Object>();
        for (int i = 0; i < 4; i++) {
            results.add(parser.nextValue());
        }

        assertEquals(expected, results);

    }

    @Test
    public void parseTaggedValueWithUnkownTag() {
        assertEquals(new TaggedValue(new Tag(new Symbol("foo", "bar")), 1), parse("#foo/bar 1"));
    }

    @Test
    public void parseTaggedInstant() {
        assertEquals(1347235200000L, ((Date)parse("#inst \"2012-09-10\"")).getTime());
    }

    @Test
    public void parseTaggedUUID() {
        assertEquals(UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"),
                parse("#uuid \"f81d4fae-7dec-11d0-a765-00a0c91e6bf6\""));
    }

    @Test(expected=NumberFormatException.class)
    public void invalidUUIDCausesException() {
        parse("#uuid \"f81d4fae-XXXX-11d0-a765-00a0c91e6bf6\"");
    }

    @Test
    public void discardedTaggedValuesDoNotCallTransformer() {
        // The given UUID is invalid, as demonstrated in the test above.
        // were the transformer for #uuid to be called despite the #_,
        // it would throw an exception and cause this test to fail.

        assertEquals(123, parse("#_ #uuid \"f81d4fae-XXXX-11d0-a765-00a0c91e6bf6\" 123"));
    }

    static Object parse(String input) {
        return parser(input).nextValue();
    }

    static Parser parser(String input) {
        return Parser.newParser(ParserConfiguration.defaultConfiguration(),
                new CharSequenceInput(input));
    }

    private Map<Object, Object> map(Object... kvs) {
        Map<Object, Object> m = new HashMap<Object, Object>();
        for (int i = 0; i < kvs.length; i += 2) {
            m.put(kvs[i], kvs[i + 1]);
        }
        return m;
    }

}
