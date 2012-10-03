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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;

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

    static final char END = 0;
    static final int BUFFER_CAPACITY = 4096;

    private final TagHandler longHandler;
    private final TagHandler bigDecimalHandler;
    private final TagHandler bigIntegerHandler;
    private final TagHandler doubleHandler;

    private Readable readable;
    private CharBuffer head = null;
    private CharBuffer tail = null;

    private int readIntoBuffer(CharBuffer buff) throws IOException {
        buff.clear();
        int n = 0;
        while (n == 0) {
            n = readable.read(buff);
        }
        buff.flip();
        assert buff.position() == 0;
        assert buff.limit() == n || buff.limit() == 0 && n < 0;
        return n;
    }

    private void initBuffers() throws IOException {
        assert head == null && tail == null;
        head = CharBuffer.allocate(BUFFER_CAPACITY);
        tail = CharBuffer.allocate(BUFFER_CAPACITY);

        if (readIntoBuffer(head) < 0) {
            tail.position(0);
            tail.limit(0);
        } else {
            readIntoBuffer(tail);
        }
    }

    private void advanceBuffers() throws IOException {
        if (head.limit() == 0) {
            return;
        }
        if (tail.limit() == 0) {
            head = tail;
            return;
        }

        final CharBuffer temp = head;
        head = tail;
        tail = temp;
        readIntoBuffer(tail);
    }

    private char nextChar() throws IOException {
        if (head == null) {
            initBuffers();
        }
        if (head.position() == head.limit()) {
            advanceBuffers();
        }
        if (head.limit() == 0) {
            return END;
        }
        return head.get();
    }

    private char curr() throws IOException {
        assert head.limit() == 0 && head.position() == 0 || head.position() > 0;
        if (head.position() > 0) {
            return head.get(head.position() - 1);
        }
        return END;
    }

    private char peek() {
        if (head.position() < head.limit()) {
            return head.get(head.position());
        }
        if (tail.limit() > 0) {
            return tail.get(0);
        }
        return END;
    }

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

        this.readable = readable;
        initBuffers();
        nextChar();
    }


    public void close() throws IOException {
        if (readable instanceof Closeable) {
            ((Closeable)readable).close();
        }
    }

    public Object nextToken() throws IOException {
        try {
            return nextToken0();
        } catch (IOException e) {
            try {
                close();
            } catch (IOException _) {
                // suppress _ in favor of e
            }
            throw e;
        }
    }

    private Object nextToken0() throws IOException {
        skipWhitespaceAndComments();
        switch(curr()) {
        case END:
            return Token.END_OF_INPUT;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
            return readSymbol();
        case 'f':
        {
            Symbol sym = readSymbol();
            return FALSE_SYMBOL.equals(sym) ? false : sym;
        }
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
            return readSymbol();
        case 'n':
        {
            Symbol sym = readSymbol();
            return NIL_SYMBOL.equals(sym) ? Token.NIL : sym;
        }
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
            return readSymbol();
        case 't':
        {
            Symbol sym = readSymbol();
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
            return readSymbol();
        case '+':
        case '-':
            if (isDigit(peek())) {
                return readNumber();
            } else {
                return readSymbol();
            }
        case ':':
            return readKeyword();
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
            return readNumber();
        case '{':
            nextChar();
            return Token.BEGIN_MAP;
        case '}':
            nextChar();
            return Token.END_MAP_OR_SET;
        case '[':
            nextChar();
            return Token.BEGIN_VECTOR;
        case ']':
            nextChar();
            return Token.END_VECTOR;
        case '(':
            nextChar();
            return Token.BEGIN_LIST;
        case ')':
            nextChar();
            return Token.END_LIST;
        case '#':
            switch(peek()) {
            case '{':
                nextChar(); nextChar();
                return Token.BEGIN_SET;
            case '_':
                nextChar(); nextChar();
                return Token.DISCARD;
            default:
                return readTag();
            }
        case '"':
            return readStringLiteral();
        case '\\':
            return readCharacterLiteral();
        default:
            throw new EdnException(
                String.format("Unexpected character '%c', \\"+"u%04x", curr(), (int)curr()));
        }
    }

    private void skipWhitespaceAndComments() throws IOException {
        skipWhitespace();
        while (curr() == ';') {
            skipComment();
            skipWhitespace();
        }
    }

    private void skipWhitespace() throws IOException {
        while (isWhitespace(curr()) && curr() != END) {
            nextChar();
        }
    }

    private void skipComment() throws IOException {
        assert curr() == ';';
        do {
            nextChar();
        } while (!isEndOfLine(curr()) && curr() != END);
    }

    private static final boolean isEndOfLine(char c) {
        return c == '\n' || c == '\r';
    }



    private char readCharacterLiteral() throws IOException {
        assert curr() == '\\';
        nextChar();
        if (isWhitespace(curr())) {
            throw new EdnException(
                "A backslash introducing character literal must not be "+
                "immediately followed by whitespace.");
        }
        StringBuilder b = new StringBuilder();
        do {
            b.append(curr());
        } while (!separatesTokens(nextChar()));
        String s = b.toString();
        if (s.length() == 1) {
            return s.charAt(0);
        } else {
            return charForName(s);
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

    private String readStringLiteral() throws IOException {
        assert curr() == '"';
        nextChar();
        StringBuffer b = new StringBuffer();
        while (curr() != '"' && curr() != END) {
            if (curr() == '\\') {
                nextChar();
                switch(curr()) {
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
                    throw new EdnException("Unsupported '"+ curr() +"' escape in string");
                }
            } else {
                b.append(curr());
            }
            nextChar();
        }
        if (curr() == '"') {
            nextChar();
        } else {
            throw new EdnException("Unclosed string literal");
        }
        return b.toString();
    }

    private Object readNumber() throws IOException {
        assert CharClassify.startsNumber(curr());
        StringBuffer digits = new StringBuffer();

        if (curr() != '+') {
            digits.append(curr());
        }
        while (isDigit(nextChar())) {
            digits.append(curr());
        }

        if (curr() == '.' || curr() == 'e' || curr() == 'E' || curr() == 'M') {
            return parseFloat(digits);
        } else {
            return parseInteger(digits);
        }
    }

    private Object parseFloat(StringBuffer digits) throws IOException {
        if (curr() == '.') {
            do {
                digits.append(curr());
            } while (isDigit(nextChar()));
        }

        if (curr() == 'e' || curr() == 'E') {
            digits.append(curr());
            nextChar();
            if (!(curr() == '-' || curr() == '+' || isDigit(curr()))) {
                throw new EdnException("Not a number: '"+ digits + curr() +"'.");
            }
            do {
                digits.append(curr());
            } while (isDigit(nextChar()));
        }

        final boolean decimal;
        if (curr() == 'M') {
            decimal = true;
            nextChar();
        } else {
            decimal = false;
        }

        if (!separatesTokens(curr())) {
            throw new EdnException("Not a number: '"+ digits + curr() +"'.");
        }

        if (decimal) {
            BigDecimal d = new BigDecimal(digits.toString());
            return bigDecimalHandler.transform(BIG_DECIMAL_TAG, d);
        } else {
            double d = Double.parseDouble(digits.toString());
            return doubleHandler.transform(DOUBLE_TAG, d);
        }
    }

    private Object parseInteger(CharSequence digits) throws IOException {
        final boolean bigint;
        if (curr() == 'N') {
            bigint = true;
            nextChar();
        } else {
            bigint = false;
        }

        if (!separatesTokens(curr())) {
            throw new EdnException("Not a number: '"+ digits + curr() +"'.");
        }

        final BigInteger n = new BigInteger(digits.toString());

        if (bigint || MIN_LONG.compareTo(n) > 0 || n.compareTo(MAX_LONG) > 0) {
            return bigIntegerHandler.transform(BIG_INTEGER_TAG, n);
        } else {
            return longHandler.transform(LONG_TAG, n.longValue());
        }
    }

    private Keyword readKeyword() throws IOException {
        assert curr() == ':';
        nextChar();
        Symbol sym = readSymbol();
        if (SLASH_SYMBOL.equals(sym)) {
            throw new EdnException("':/' is not a valid keyword.");
        }
        return Keyword.newKeyword(sym);
    }

    private Tag readTag() throws IOException {
        assert curr() == '#';
        nextChar();
        return newTag(readSymbol());
    }


    private Symbol readSymbol() throws IOException {
        assert CharClassify.symbolStart(curr());

        StringBuilder b = new StringBuilder();
        int n = 0;
        int p = Integer.MIN_VALUE;
        do {
            if (curr() == '/') {
                n += 1;
                p = b.length();
            }
            b.append(curr());
            nextChar();
        } while (!separatesTokens(curr()));

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
