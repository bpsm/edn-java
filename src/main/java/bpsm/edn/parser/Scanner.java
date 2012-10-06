// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.Symbol.newSymbol;
import static bpsm.edn.Tag.newTag;
import static bpsm.edn.parser.Parser.Config.BIG_DECIMAL_TAG;
import static bpsm.edn.parser.Parser.Config.BIG_INTEGER_TAG;
import static bpsm.edn.parser.Parser.Config.DOUBLE_TAG;
import static bpsm.edn.parser.Parser.Config.LONG_TAG;
import static bpsm.edn.util.CharClassify.isDigit;
import static bpsm.edn.util.CharClassify.isWhitespace;
import static bpsm.edn.util.CharClassify.separatesTokens;

import java.io.Closeable;
import java.io.IOException;
import java.io.PushbackReader;
import java.math.BigDecimal;
import java.math.BigInteger;

import bpsm.edn.EdnException;
import bpsm.edn.Keyword;
import bpsm.edn.Symbol;
import bpsm.edn.Tag;
import bpsm.edn.util.CharClassify;


class Scanner implements Closeable {

    static final Symbol NIL_SYMBOL = newSymbol(null, "nil");
    static final Symbol TRUE_SYMBOL = newSymbol(null, "true");
    static final Symbol FALSE_SYMBOL = newSymbol(null, "false");
    static final Symbol SLASH_SYMBOL = newSymbol(null, "/");

    static final int END = -1;

    private final TagHandler longHandler;
    private final TagHandler bigDecimalHandler;
    private final TagHandler bigIntegerHandler;
    private final TagHandler doubleHandler;

    private PushbackReader pbr;

    /**
     * Scanner may throw an IOException during construction, in which case
     * an attempt will be made to close Reader cleanly.
     * @param reader
     * @throws IOException
     */
    Scanner(Parser.Config cfg, Readable readable) throws IOException {
        if (cfg == null) {
            throw new IllegalArgumentException("cfg must not be null");
        }
        if (readable == null) {
            throw new IllegalArgumentException("readable must not be null");
        }

        this.longHandler = cfg.getTagHandler(LONG_TAG);
        this.bigIntegerHandler = cfg.getTagHandler(BIG_INTEGER_TAG);
        this.doubleHandler = cfg.getTagHandler(DOUBLE_TAG);
        this.bigDecimalHandler = cfg.getTagHandler(BIG_DECIMAL_TAG);

        this.pbr = Readers.pushbackReader(readable);
    }

    public void close() throws IOException {
        pbr.close();
    }

    public Object nextToken() throws IOException {
        try {
            return nextToken(pbr.read());
        } catch (IOException e) {
            try {
                close();
            } catch (IOException _) {
                // suppress _ in favor of e
            }
            throw e;
        }
    }

    private Object nextToken(int curr) throws IOException {
        curr = skipWhitespaceAndComments(curr);
        switch(curr) {
        case END:
            return Token.END_OF_INPUT;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
            return readSymbol(curr);
        case 'f':
        {
            Symbol sym = readSymbol(curr);
            return FALSE_SYMBOL.equals(sym) ? false : sym;
        }
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
            return readSymbol(curr);
        case 'n':
        {
            Symbol sym = readSymbol(curr);
            return NIL_SYMBOL.equals(sym) ? Token.NIL : sym;
        }
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
            return readSymbol(curr);
        case 't':
        {
            Symbol sym = readSymbol(curr);
            return TRUE_SYMBOL.equals(sym) ? true : sym;
        }
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '*':
        case '!':
        case '_':
        case '?':
        case '/':
        case '.':
            return readSymbol(curr);
        case '+':
        case '-': {
            int peek = pbr.read();
            if (peek == END) {
                return readSymbol(curr);
            } else {
                char p = (char) peek;
                pbr.unread(p);
                if (isDigit(p)) {
                    return readNumber(curr);
                } else {
                    return readSymbol(curr);
                }
            }}
        case ':':
            return readKeyword(curr);
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return readNumber(curr);
        case '{':
            return Token.BEGIN_MAP;
        case '}':
            return Token.END_MAP_OR_SET;
        case '[':
            return Token.BEGIN_VECTOR;
        case ']':
            return Token.END_VECTOR;
        case '(':
            return Token.BEGIN_LIST;
        case ')':
            return Token.END_LIST;
        case '#': {
            int peek = pbr.read();
            switch(peek) {
            case END:
                throw new EdnException("Unexpected end of input following '#'");
            case '{':
                return Token.BEGIN_SET;
            case '_':
                return Token.DISCARD;
            default:
                pbr.unread((char)peek);
                return readTag(curr);
            }}
        case '"':
            return readStringLiteral(curr);
        case '\\':
            return readCharacterLiteral(curr);
        default:
            throw new EdnException(
                String.format("Unexpected character '%c', \\"+"u%04x",
                        (char)curr, curr));
        }
    }

    private int skipWhitespaceAndComments(int curr) throws IOException {
        curr = skipWhitespace(curr);
        while (curr == ';') {
            curr = skipComment(curr);
            curr = skipWhitespace(curr);
        }
        return curr;
    }

    private int skipWhitespace(int curr) throws IOException {
        while (curr != END && isWhitespace((char)curr)) {
            curr = pbr.read();
        }
        return curr;
    }

    private int skipComment(int curr) throws IOException {
        assert curr == ';';
        do {
            curr = pbr.read();
        } while (curr != END && curr != '\n' && curr != '\r');
        return curr;
    }

    private char readCharacterLiteral(int curr) throws IOException {
        assert curr == '\\';
        curr = pbr.read();
        if (curr == END) {
            throw new EdnException(
                    "Unexpected end of input following '\'");
        } else if (isWhitespace((char)curr) && curr != ',') {
            throw new EdnException(
                "A backslash introducing character literal must not be "+
                "immediately followed by whitespace.");
        }
        StringBuilder b = new StringBuilder();
        do {
            b.append((char)curr);
            curr = pbr.read();
        } while (curr != END && !separatesTokens((char)curr));
        if (curr != END) {
            pbr.unread((char)curr);
        }
        if (b.length() == 1) {
            return b.charAt(0);
        } else {
            return charForName(b.toString());
        }
    }

    private static char charForName(String name) {
        switch (name.charAt(0)) {
        case 'n':
            if ("newline".equals(name)) {
                return '\n';
            }
            // fall through
        case 's':
            if ("space".equals(name)) {
                return ' ';
            }
            // fall through
        case 't':
            if ("tab".equals(name)) {
                return '\t';
            }
            // fall through
        case 'b':
            if ("backspace".equals(name)) {
                return '\b';
            }
            // fall through
        case 'f':
            if ("formfeed".equals(name)) {
                return '\f';
            }
            // fall through
        case 'r':
            if ("return".equals(name)) {
                return '\r';
            }
            // fall through
        default:
            throw new EdnException(
                "The character \\"+ name +" was not recognized.");
        }
    }

    private String readStringLiteral(int curr) throws IOException {
        assert curr == '"';
        StringBuffer b = new StringBuffer();
        for (;;) {
            curr = pbr.read();
            switch (curr) {
            case END:
                throw new EdnException(
                        "Unexpected end of input in string literal");
            case '"':
                return b.toString();
            case '\\':
                curr = pbr.read();
                switch (curr) {
                case END:
                    throw new EdnException(
                            "Unexpected end of input in string literal");
                case 'b':
                    b.append('\b');
                    break;
                case 't':
                    b.append('\t');
                    break;
                case 'n':
                    b.append('\n');
                    break;
                case 'f':
                    b.append('\f');
                    break;
                case 'r':
                    b.append('\r');
                    break;
                case '"':
                    b.append('"');
                    break;
                case '\'':
                    b.append('\'');
                    break;
                case '\\':
                    b.append('\\');
                    break;
                default:
                    throw new EdnException("Unsupported '"+ ((char)curr)
                            +"' escape in string");
                }
                break;
            default:
                b.append((char)curr);
            }
        }
    }

    private Object readNumber(int curr) throws IOException {
        assert curr != END && CharClassify.startsNumber((char)curr);
        StringBuffer digits = new StringBuffer();

        if (curr != '+') {
            digits.append((char)curr);
        }
        curr = pbr.read();
        while (curr != END && isDigit((char)curr)) {
            digits.append((char)curr);
            curr = pbr.read();
        }

        if (curr == '.' || curr == 'e' || curr == 'E' || curr == 'M') {
            return parseFloat(curr, digits);
        } else {
            return parseInteger(curr, digits);
        }
    }

    private Object parseFloat(int curr, StringBuffer digits) throws IOException {
        assert (curr == '.' || curr == 'e' || curr == 'E' || curr == 'M');
        if (curr == '.') {
            do {
                digits.append((char)curr);
                curr = pbr.read();
            } while (curr != END && isDigit((char) curr));
        }

        if (curr == 'e' || curr == 'E') {
            digits.append((char)curr);
            curr = pbr.read();
            if (curr == END) {
                throw new EdnException("Unexpected end of input in numeric literal");
            }
            if (!(curr == '-' || curr == '+' || isDigit((char)curr))) {
                throw new EdnException("Not a number: '"+ digits + ((char)curr) +"'.");
            }
            do {
                digits.append((char)curr);
                curr = pbr.read();
            } while (curr != END && isDigit((char)curr));
        }

        final boolean decimal;
        if (curr == 'M') {
            decimal = true;
            curr = pbr.read();
        } else {
            decimal = false;
        }

        if (curr != END && !separatesTokens((char)curr)) {
            throw new EdnException("Not a number: '"+ digits + ((char)curr) +"'.");
        }
        if (curr != END) {
            pbr.unread((char)curr);
        }

        if (decimal) {
            BigDecimal d = new BigDecimal(digits.toString());
            return bigDecimalHandler.transform(BIG_DECIMAL_TAG, d);
        } else {
            double d = Double.parseDouble(digits.toString());
            return doubleHandler.transform(DOUBLE_TAG, d);
        }
    }

    private Object parseInteger(int curr, CharSequence digits) throws IOException {
        final boolean bigint;
        if (curr == 'N') {
            bigint = true;
            curr = pbr.read();
        } else {
            bigint = false;
        }

        if (curr != END && !separatesTokens((char)curr)) {
            throw new EdnException("Not a number: '"+ digits + ((char)curr) +"'.");
        }
        if (curr != END) {
            pbr.unread((char)curr);
        }

        final BigInteger n = new BigInteger(digits.toString());

        if (bigint || MIN_LONG.compareTo(n) > 0 || n.compareTo(MAX_LONG) > 0) {
            return bigIntegerHandler.transform(BIG_INTEGER_TAG, n);
        } else {
            return longHandler.transform(LONG_TAG, n.longValue());
        }
    }

    private Keyword readKeyword(int curr) throws IOException {
        assert curr == ':';
        curr = pbr.read();
        if (curr == END) {
            throw new EdnException(
                    "Unexpected end of input while reading keyword");
        }
        Symbol sym = readSymbol(curr);
        if (SLASH_SYMBOL.equals(sym)) {
            throw new EdnException("':/' is not a valid keyword.");
        }
        return Keyword.newKeyword(sym);
    }

    private Tag readTag(int curr) throws IOException {
        assert curr == '#';
        curr = pbr.read();
        if (curr == END) {
            throw new EdnException("Unexpected end of input while reading tag");
        }
        return newTag(readSymbol(curr));
    }


    private Symbol readSymbol(int curr) throws IOException {
        assert curr != END && CharClassify.symbolStart((char)curr);
        StringBuilder b = new StringBuilder();
        int n = 0;
        int p = Integer.MIN_VALUE;
        do {
            if (curr == '/') {
                n += 1;
                p = b.length();
            }
            b.append((char)curr);
            curr = pbr.read();
        } while (curr != END && !separatesTokens((char)curr));
        if (curr != END) {
            pbr.unread((char)curr);
        }

        validateUseOfSlash(b, n, p);
        return makeSymbol(b, n, p);
    }

    private Symbol makeSymbol(StringBuilder b, int slashCount, int slashPos) {
        if (slashCount == 0) {
            return newSymbol(null, b.toString());
        } else {
            assert slashCount == 1;
            if (slashPos == 0) {
                assert b.length() == 1 && b.charAt(0) == '/';
                return newSymbol(null, b.toString());
            } else {
                return newSymbol(b.substring(0, slashPos), b.substring(slashPos+1));
            }
        }
    }

    private void validateUseOfSlash(CharSequence s, int slashCount, int lastSlashPos) {
        if (slashCount > 1) {
            throw new EdnException(
                "The name '"+ s +"' must not contain more than one '/'.");
        }
        if (lastSlashPos == 0 && s.length() > 1) {
            throw new EdnException(
                "The name '"+ s +"' must not start with '/'.");
        }
        if (s.length() > 1) {
            if (lastSlashPos == s.length() - 1) {
                throw new EdnException(
                    "The name '"+ s +"' must not end with '/'.");
            }
        }
    }

    private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);


}
