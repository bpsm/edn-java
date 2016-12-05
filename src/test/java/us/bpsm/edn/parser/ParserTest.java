// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static us.bpsm.edn.Symbol.newSymbol;
import static us.bpsm.edn.Tag.newTag;
import static us.bpsm.edn.TaggedValue.newTaggedValue;
import static us.bpsm.edn.parser.Parser.Config.BIG_DECIMAL_TAG;
import static us.bpsm.edn.parser.Parser.Config.BIG_INTEGER_TAG;
import static us.bpsm.edn.parser.Parser.Config.DOUBLE_TAG;
import static us.bpsm.edn.parser.Parser.Config.LONG_TAG;
import static us.bpsm.edn.parser.Parsers.defaultConfiguration;
import static us.bpsm.edn.parser.Parsers.newParserConfigBuilder;

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
import java.util.Set;
import java.util.UUID;
import java.util.RandomAccess;

import org.junit.Test;

import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;



public class ParserTest {

    @Test
    public void parseEdnSample() throws IOException {
        Parseable pbr = Parsers.newParseable(IOUtil.stringFromResource("us/bpsm/edn/edn-sample.txt"));
        Parser parser = Parsers.newParser(Parsers.defaultConfiguration());

        @SuppressWarnings("unchecked")
        List<Object> expected = Arrays.asList(
            map(ScannerTest.key("keyword"), ScannerTest.sym("symbol"), 1L,
                2.0d, new BigInteger("3"), new BigDecimal("4.0")),
                Arrays.asList(1L, 1L, 2L, 3L, 5L, 8L),
                new HashSet<Object>(Arrays.asList('\n', '\t')),
                Arrays.asList(Arrays.asList(Arrays.asList(true, false, null))));

        List<Object> results = new ArrayList<Object>();
        for (int i = 0; i < 4; i++) {
            results.add(parser.nextValue(pbr));
        }
        assertEquals(expected, results);
    }

    @Test
    public void parseTaggedValueWithUnkownTag() {
        assertEquals(newTaggedValue(newTag(newSymbol("foo", "bar")), 1L), parse("#foo/bar 1"));
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

    private static final String INVALID_UUID = "#uuid \"f81d4fae-XXXX-11d0-a765-00a0c91e6bf6\"";

    @Test(expected=NumberFormatException.class)
    public void invalidUUIDCausesException() {
        parse(INVALID_UUID);
    }

    @Test
    public void discardedTaggedValuesDoNotCallTransformer() {
        // The given UUID is invalid, as demonstrated in the test above.
        // were the transformer for #uuid to be called despite the #_,
        // it would throw an exception and cause this test to fail.

        assertEquals(123L, parse("#_ " + INVALID_UUID + " 123"));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void parserShouldReturnUnmodifiableListByDefault() {
        ((List<?>)parse("(1)")).remove(0);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void parserShouldReturnUnmodifiableVectorByDefault() {
        ((List<?>)parse("[1]")).remove(0);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void parserShouldReturnUnmodifiableSetByDefault() {
        ((Set<?>)parse("#{1}")).remove(1);

    }

    @Test(expected=UnsupportedOperationException.class)
    public void parserShouldReturnUnmodifiableMapByDefault() {
        ((Map<?,?>)parse("{1,-1}")).remove(1);

    }

    @Test
    public void integersParseAsLongByDefault() {
        List<?> expected = Arrays.asList(
            Long.MIN_VALUE, (long)Integer.MIN_VALUE,
            -1L, 0L, 1L,
            (long)Integer.MAX_VALUE, Long.MAX_VALUE);
        List<?> results = (List<?>)parse("[" +
            Long.MIN_VALUE + ", " + Integer.MIN_VALUE +
            ", -1, 0, 1, " +
            Integer.MAX_VALUE + ", " + Long.MAX_VALUE + "]");
        // In Java Integer and Long are never equal(), even if they have
        // the same value.
        assertEquals(expected, results);
    }

    @Test
    public void integersAutoPromoteToBigIfTooBig() {
        BigInteger tooNegative = BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE);
        BigInteger tooPositive = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        List<?> expected = Arrays.asList(tooNegative, tooPositive);
        List<?> results = (List<?>)parse("[" + tooNegative +" " + tooPositive + "]");
        assertEquals(expected, results);
    }

    @Test
    public void canCustomizeParsingOfInteger() {
        Parser.Config cfg = newParserConfigBuilder()
            .putTagHandler(LONG_TAG, new TagHandler() {
                public Object transform(Tag tag, Object value) {
                    return Integer.valueOf(((Long)value).intValue());
                }})
                .putTagHandler(BIG_INTEGER_TAG, new TagHandler() {
                    public Object transform(Tag tag, Object value) {
                        return Integer.valueOf(((BigInteger)value).intValue());
                    }})
                    .build();
        List<Integer> expected = Arrays.asList(-1, 0, 0, 1);
        List<?> results = (List<?>) parse(cfg, "[-1N, 0, 0N, 1]");
        assertEquals(expected, results);
    }

    @Test
    public void canCustomizeParsingOfFloats() {
        Parser.Config cfg = newParserConfigBuilder()
            .putTagHandler(DOUBLE_TAG, new TagHandler() {
                public Object transform(Tag tag, Object value) {
                    Double d = (Double) value;
                    return d * 2.0;
                }})
                .putTagHandler(BIG_DECIMAL_TAG, new TagHandler() {
                    public Object transform(Tag tag, Object value) {
                        BigDecimal d = (BigDecimal)value;
                        return d.multiply(BigDecimal.TEN);
                    }})
                    .build();
        @SuppressWarnings("unchecked")
        List<?> expected = Arrays.asList(BigDecimal.TEN.negate(),
            BigDecimal.ZERO,
            BigDecimal.TEN,
            -2.0d, 0.0d, 2.0d);
        List<?> results = (List<?>) parse(cfg, "[-1M, 0M, 1M, -1.0, 0.0, 1.0]");
        assertEquals(expected, results);
    }

    @Test
    public void issue32() {
        assertFalse(parse("()") instanceof RandomAccess);
        assertTrue(parse("[]") instanceof RandomAccess);
        assertFalse(parse("(1)") instanceof RandomAccess);
        assertTrue(parse("[1]") instanceof RandomAccess);
    }

    @Test(expected=EdnSyntaxException.class)
    public void duplicateMapKeys() {
        Parseable pbr = Parsers.newParseable(IOUtil.stringFromResource("us/bpsm/edn/duplicate-map-keys.edn"));
        Parser parser = Parsers.newParser(Parsers.defaultConfiguration());
        Object o = parser.nextValue(pbr);
    }

    //@Test
    public void performanceOfInstantParsing() {
        StringBuilder b = new StringBuilder();
        for (int h = -12; h <= 12; h++) {
            b.append("#inst ")
            .append('"')
            .append("2012-11-25T10:11:12.343")
            .append(String.format("%+03d", h))
            .append(":00")
            .append('"')
            .append(' ');
        }
        for (int i = 0; i <  9; i++) {
            b.append(b.toString());
        }
        String txt = "[" + b.toString() + "]";
        long ns = System.nanoTime();
        List<?> result = (List<?>) parse(txt);
        ns = System.nanoTime() - ns;
        long ms = ns / 1000000;
        System.out.printf("%d insts took %d ms (%1.2f ms/inst)\n",
            result.size(), ms, (1.0*ms)/result.size());
    }

    static Object parse(String input) {
        return parse(defaultConfiguration(), input);
    }

    static Object parse(Parser.Config cfg, String input) {
        return Parsers.newParser(cfg).nextValue(Parsers.newParseable(input));
    }

    private Map<Object, Object> map(Object... kvs) {
        Map<Object, Object> m = new HashMap<Object, Object>();
        for (int i = 0; i < kvs.length; i += 2) {
            m.put(kvs[i], kvs[i + 1]);
        }
        return m;
    }

}
