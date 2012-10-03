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
import java.io.Reader;
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

    static final char END = 0;

    private final TagHandler longHandler;
    private final TagHandler bigDecimalHandler;
    private final TagHandler bigIntegerHandler;
    private final TagHandler doubleHandler;

    private Reader reader;
    private char curr = 0;
    private char peek = 0;


    private char getCurr() {
        return curr;
    }

    private void setCurr(char curr) {
        this.curr = curr;
    }

    private char getPeek() {
        return peek;
    }

    private void setPeek(char peek) {
        this.peek = peek;
    }

    /**
     * Scanner may throw an IOException during construction, in which case
     * an attempt will be made to close Reader cleanly.
     * @param reader
     * @throws IOException
     */
    Scanner(Parser.Config cfg, Reader reader) throws IOException {
        if (cfg == null) {
            throw new IllegalArgumentException("cfg must not be null");
        }
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }

        this.longHandler = cfg.getTagHandler(LONG_TAG);
        this.bigIntegerHandler = cfg.getTagHandler(BIG_INTEGER_TAG);
        this.doubleHandler = cfg.getTagHandler(DOUBLE_TAG);
        this.bigDecimalHandler = cfg.getTagHandler(BIG_DECIMAL_TAG);

        this.reader = reader;
        try {
            this.setCurr((char) Math.max(0, reader.read()));
            if (getCurr() != 0) {
                this.setPeek((char) Math.max(0, reader.read()));
            }
        } catch (IOException e) {
            try {
                reader.close();
            } catch (IOException _) {
                // suppress _ in favor of e
            }
            throw e;
        }
    }

    private char nextChar() throws IOException {
        setCurr(getPeek());
        if (getCurr() != 0) {
            setPeek((char) Math.max(0,  reader.read()));
        }
        return getCurr();
    }

    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
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
        switch(getCurr()) {
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
            if (isDigit(getPeek())) {
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
            switch(getPeek()) {
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
                    String.format("Unexpected character '%c', \\"+"u%04x", getCurr(), (int)getCurr()));
        }
    }

    private void skipWhitespaceAndComments() throws IOException {
        skipWhitespace();
        while (getCurr() == ';') {
            skipComment();
            skipWhitespace();
        }
    }

    private void skipWhitespace() throws IOException {
        while (isWhitespace(getCurr()) && getCurr() != END) {
            nextChar();
        }
    }

    private void skipComment() throws IOException {
        assert getCurr() == ';';
        do {
            nextChar();
        } while (!isEndOfLine(getCurr()) && getCurr() != END);
    }

    private static final boolean isEndOfLine(char c) {
        return c == '\n' || c == '\r';
    }



    private char readCharacterLiteral() throws IOException {
        assert getCurr() == '\\';
        nextChar();
        if (isWhitespace(getCurr())) {
            throw new EdnException(
                    "A backslash introducing character literal must not be "+
                    "immediately followed by whitespace.");
        }
        StringBuilder b = new StringBuilder();
        do {
            b.append(getCurr());
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
        assert getCurr() == '"';
        nextChar();
        StringBuffer b = new StringBuffer();
        while (getCurr() != '"' && getCurr() != END) {
            if (getCurr() == '\\') {
                nextChar();
                switch(getCurr()) {
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
                    throw new EdnException("Unsupported '"+ getCurr() +"' escape in string");
                }
            } else {
                b.append(getCurr());
            }
            nextChar();
        }
        if (getCurr() == '"') {
            nextChar();
        } else {
            throw new EdnException("Unclosed string literal");
        }
        return b.toString();
    }

    private Object readNumber() throws IOException {
        assert CharClassify.startsNumber(getCurr());
        StringBuffer digits = new StringBuffer();

        if (getCurr() != '+') {
            digits.append(getCurr());
        }
        while (isDigit(nextChar())) {
            digits.append(getCurr());
        }

        if (getCurr() == '.' || getCurr() == 'e' || getCurr() == 'E' || getCurr() == 'M') {
            return parseFloat(digits);
        } else {
            return parseInteger(digits);
        }
    }

    private Object parseFloat(StringBuffer digits) throws IOException {
        if (getCurr() == '.') {
            do {
                digits.append(getCurr());
            } while (isDigit(nextChar()));
        }

        if (getCurr() == 'e' || getCurr() == 'E') {
            digits.append(getCurr());
            nextChar();
            if (!(getCurr() == '-' || getCurr() == '+' || isDigit(getCurr()))) {
                throw new EdnException("Not a number: '"+ digits + getCurr() +"'.");
            }
            do {
                digits.append(getCurr());
            } while (isDigit(nextChar()));
        }

        final boolean decimal;
        if (getCurr() == 'M') {
            decimal = true;
            nextChar();
        } else {
            decimal = false;
        }

        if (!separatesTokens(getCurr())) {
            throw new EdnException("Not a number: '"+ digits + getCurr() +"'.");
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
        if (getCurr() == 'N') {
            bigint = true;
            nextChar();
        } else {
            bigint = false;
        }

        if (!separatesTokens(getCurr())) {
            throw new EdnException("Not a number: '"+ digits + getCurr() +"'.");
        }

        final BigInteger n = new BigInteger(digits.toString());

        if (bigint || MIN_LONG.compareTo(n) > 0 || n.compareTo(MAX_LONG) > 0) {
            return bigIntegerHandler.transform(BIG_INTEGER_TAG, n);
        } else {
            return longHandler.transform(LONG_TAG, n.longValue());
        }
    }

    private Keyword readKeyword() throws IOException {
        assert getCurr() == ':';
        nextChar();
        Symbol sym = readSymbol();
        if (SLASH_SYMBOL.equals(sym)) {
            throw new EdnException("':/' is not a valid keyword.");
        }
        return Keyword.newKeyword(sym);
    }

    private Tag readTag() throws IOException {
        assert getCurr() == '#';
        nextChar();
        return newTag(readSymbol());
    }


    private Symbol readSymbol() throws IOException {
        assert CharClassify.symbolStart(getCurr());

        StringBuilder b = new StringBuilder();
        int n = 0;
        int p = Integer.MIN_VALUE;
        do {
          if (getCurr() == '/') {
              n += 1;
              p = b.length();
          }
          b.append(getCurr());
          nextChar();
        } while (!separatesTokens(getCurr()));

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
