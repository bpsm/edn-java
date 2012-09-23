package bpsm.edn.parser;

import static bpsm.edn.parser.CharSequenceReader.newCharSequenceReader;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.IdentityHashMap;

import org.junit.Assert;
import org.junit.Test;

import bpsm.edn.model.Keyword;
import bpsm.edn.model.Symbol;

public class TestScanner {

    @Test
    public void testEmpty() {
        assertEquals(Token.END_OF_INPUT, scan(""));
    }

    @Test
    public void testNil() {
        assertEquals(Token.NIL, scan("nil"));
    }

    @Test
    public void testTrue() {
        assertEquals(true, scan("true"));
    }

    @Test
    public void testFalse() {
        assertEquals(false, scan("false"));
    }

    @Test
    public void symbolWithoutPrefix() {
        assertEquals(sym("foo"), scan("foo"));
    }

    @Test
    public void symbolSlash() {
        assertEquals(sym("/"), scan("/"));
    }

    @Test
    public void symbolWithPrefix() {
        assertEquals(sym("a", "b"), scan("a/b"));
    }

    @Test(expected=EdnException.class)
    public void symbolHasTooManySlashes() {
        scan("a/b/c");
    }

    @Test(expected=EdnException.class)
    public void symbolEndsInSlash() {
        scan("a/");
    }

    @Test(expected=EdnException.class)
    public void symbolStartsWithSlash() {
        scan("/a");
    }

    @Test(expected=EdnException.class)
    public void symbolStartsWithDotDigit() {
        scan(".4symbol");
    }
    
    @Test(expected=EdnException.class)
    public void symbolStartsWithDashDigit() {
        scan("-4symbol");
    }
    
    @Test(expected=EdnException.class)
    public void symbolStartsWithPlusDigit() {
        scan("+4symbol");
    }

    @Test
    public void keywordWithoutPrefix() {
        assertEquals(key("+"), scan(":+"));
    }
    
    /** issue 5 */
    @Test(expected=EdnException.class)
    public void colonSlashIsNotAValidKeyword() {
        scan(":/");
    }

    @Test
    public void keywordWithPrefix() {
        assertEquals(key("foo:bar", ".baz"), scan(":foo:bar/.baz"));
    }

    @Test
    public void beginList() {
        assertEquals(Token.BEGIN_LIST, scan("("));
    }

    @Test
    public void endList() {
        assertEquals(Token.END_LIST, scan(")"));
    }

    @Test
    public void beginVector() {
        assertEquals(Token.BEGIN_VECTOR, scan("["));
    }

    @Test
    public void endVector() {
        assertEquals(Token.END_VECTOR, scan("]"));
    }

    @Test
    public void beginMap() {
        assertEquals(Token.BEGIN_MAP, scan("{"));
    }

    @Test
    public void beginSet() {
        assertEquals(Token.BEGIN_SET, scan("#{"));
    }

    @Test
    public void endMapOrSet() {
        assertEquals(Token.END_MAP_OR_SET, scan("}"));
    }

    @Test
    public void discard() {
        assertEquals(Token.DISCARD, scan("#_"));
    }

    @Test
    public void comment() {
        assertEquals(Token.NIL, scan("; 1\n ; 2\r\nnil"));
    }

    @Test
    public void zero() {
        assertEquals(0, scan("0"));
    }

    @Test
    public void maxLong() {
        assertEquals(9223372036854775807L, scan("9223372036854775807"));
    }

    @Test
    public void minLong() {
        assertEquals(-9223372036854775808L, scan("-9223372036854775808"));
    }

    @Test
    public void maxInteger() {
        assertEquals(2147483647, scan("2147483647"));
        assertEquals(2147483647, scan("+2147483647"));
    }

    @Test
    public void minInteger() {
        assertEquals(-2147483648, scan("-2147483648"));
    }

    @Test
    public void bigIntegerAutopromote() {
        assertEquals(new BigInteger("9223372036854775808"), scan("9223372036854775808"));
        assertEquals(new BigInteger("-9223372036854775809"), scan("-9223372036854775809"));
    }

    @Test
    public void bigIntegerRequested() {
        assertEquals(BigInteger.valueOf(1), scan("1N"));
    }

    @Test
    public void floatWithFraction() {
        assertEquals(1.23456d, scan("1.23456"));
    }

    @Test
    public void floatWithExponent() {
        assertEquals(123456e-10d, scan("123456e-10"));
    }

    @Test
    public void floatWithFractionAndExponent() {
        assertEquals(-1.23456e3d, scan("-1.23456E3"));
        assertEquals(1.23456e3d, scan("+1.23456E3"));
        assertEquals(1.23456e3d, scan("1.23456E3"));
    }

    @Test
    public void decimalWithFraction() {
        assertEquals(new BigDecimal("1.23456"), scan("1.23456M"));
    }

    @Test
    public void decimalWithExponent() {
        assertEquals(new BigDecimal("123456e-10"), scan("123456e-10M"));
    }

    @Test
    public void decimalWithFractionAndExponent() {
        assertEquals(new BigDecimal("-1.23456e3"), scan("-1.23456E3M"));
    }

    @Test
    public void emptyString() {
        assertEquals("", scan("\"\""));
    }

    @Test
    public void simpleStringEscapes() {
        assertEquals("\t\n\r\f\"\'\b\\",
                scan("\"\\t\\n\\r\\f\\\"\\\'\\b\\\\\""));
    }

    @Test
    public void namedCharacters() {
        assertEquals('\n', scan("\\newline"));
        assertEquals('\t', scan("\\tab"));
        assertEquals('\f', scan("\\formfeed"));
        assertEquals('\r', scan("\\return"));
        assertEquals(' ', scan("\\space"));
        assertEquals('\b', scan("\\backspace"));
    }

    @Test
    public void sequenceOfTokens() throws IOException {
        String txt = "; comment\n" +
                "\t\n" +
                "true false nil \\#{:keyword  [1 2N 3.0 4.0M]}symbol\n" +
                "\\newline \"some text\"\\x ; another comment\n" +
                "() #{-42}";
        Object[] expected = {
                true, false, Token.NIL, '#',
                Token.BEGIN_MAP, key("keyword"),
                Token.BEGIN_VECTOR, 1, BigInteger.valueOf(2),
                3.0d, new BigDecimal("4.0"), Token.END_VECTOR,
                Token.END_MAP_OR_SET, sym("symbol"), '\n',
                "some text", 'x', Token.BEGIN_LIST, Token.END_LIST,
                Token.BEGIN_SET, -42, Token.END_MAP_OR_SET,
                Token.END_OF_INPUT
        };
        Scanner s = scanner(txt);
        for (Object o: expected) {
            assertEquals(o, s.nextToken());
        }
    }
    
    @Test
    public void testInterning() throws IOException {
        
        // By default we intern keywords and the empty string.
        String txt = ":a b \"\" \"hi\" :a b \"\" \"hi\"";
        Scanner s = scanner(txt);
        IdentityHashMap<Object, Object> m = new IdentityHashMap<Object,Object>();
        for (Object o = s.nextToken(); o != Token.END_OF_INPUT; o = s.nextToken()) {
            m.put(o, o);
        }
        assertEquals(6, m.size());
        
        // We can customize the configuration to also intern all symbols
        // and all strings
        ParserConfiguration cfg = ParserConfiguration.builder()
                .shouldInternSymbols(true)
                .setMaxInternedStringLength(Integer.MAX_VALUE)
                .build();
        s = scanner(cfg, txt);
        m = new IdentityHashMap<Object,Object>();
        for (Object o = s.nextToken(); o != Token.END_OF_INPUT; o = s.nextToken()) {
            m.put(o, o);
        }
        assertEquals(4, m.size());
        
        // We can disable interning entirely, if we like
        cfg = ParserConfiguration.builder()
                .shouldInternKeywords(false)
                .setMaxInternedStringLength(-1)
                .build();
        s = scanner(cfg, txt);
        m = new IdentityHashMap<Object,Object>();
        for (Object o = s.nextToken(); o != Token.END_OF_INPUT; o = s.nextToken()) {
            m.put(o, o);
        }
        assertEquals(8, m.size());
        
        // We can choose to intern only strings up to a given maximum length
        cfg = ParserConfiguration.builder()
                .setMaxInternedStringLength(3)
                .build();
        s = scanner(cfg, "\"\" \"\" \"a\" \"a\" \"ab\" \"ab\" \"abc\"  \"abc\"  \"abcd\"  \"abcd\" ");
        m = new IdentityHashMap<Object,Object>();
        for (Object o = s.nextToken(); o != Token.END_OF_INPUT; o = s.nextToken()) {
            m.put(o, o);
        }
        assertEquals(6, m.size());
    }

    @Test
    public void simpleStringWithLinebreak() {
        assertEquals("\n", scan("\"\n\""));
    }
    
    @Test
    public void keywordsShouldBeInternedByScanner() throws IOException {
        Scanner s = scanner(":foo :foo");
        Assert.assertSame(s.nextToken(), s.nextToken());
    }

    static Object scan(String input) {
        try {
            return scanner(input).nextToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Scanner scanner(String input) {
        return scanner(ParserConfiguration.defaultConfiguration(), input);
    }
    
    static Scanner scanner(ParserConfiguration cfg, String input) {
        try {
            return new Scanner(cfg, newCharSequenceReader(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Symbol sym(String name) {
        return new Symbol(null, name);
    }

    static Symbol sym(String prefix, String name) {
        return new Symbol(prefix, name);
    }

    static Keyword key(String name) {
        return new Keyword(sym(null, name));
    }

    static Keyword key(String prefix, String name) {
        return new Keyword(sym(prefix, name));
    }


}
