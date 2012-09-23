// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.parser.util.CharClassify.isDigit;
import static bpsm.edn.parser.util.CharClassify.isWhitespace;
import static bpsm.edn.parser.util.CharClassify.separatesTokens;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import bpsm.edn.model.Keyword;
import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.parser.util.CharClassify;


class Scanner implements Closeable {

    static final Symbol NIL_SYMBOL = new Symbol(null, "nil");
    static final Symbol TRUE_SYMBOL = new Symbol(null, "true");
    static final Symbol FALSE_SYMBOL = new Symbol(null, "false");
    static final Symbol SLASH_SYMBOL = new Symbol(null, "/");
    
    static final char END = 0;

    private static final Interner<Keyword> keywords = Interners.newKeywordInterner(true);
    private static final Interner<Symbol> symbols= Interners.newSymbolInterner(false);
    private static final Interner<String> strings = Interners.newStringInterner(0);
    
    private Reader reader;
    private char curr = 0;
    private char peek = 0;

    /**
     * Scanner may throw an IOException during construction, in which case
     * an attempt will be made to close Reader cleanly.
     * @param reader
     * @throws IOException
     */
    Scanner(ParserConfiguration cfg, Reader reader) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        
        this.reader = reader;
        try {
            this.curr = (char) Math.max(0, reader.read());
            if (curr != 0) {
                this.peek = (char) Math.max(0, reader.read());
            }
        } catch (IOException e) {
            try {
                reader.close();
            } catch (IOException _) {
                // TODO: handle exception
            }
            throw e;
        }
    }

    private char nextChar() throws IOException {
        curr = peek;
        if (curr != 0) {
            peek = (char) Math.max(0,  reader.read());
        }
        return curr;
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
                // TODO: handle exception
            }
            throw e;
        }
    }

    private Object nextToken0() throws IOException {
        skipWhitespaceAndComments();
        switch(curr) {
        case END:
            return Token.END_OF_INPUT;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
            return symbols.intern(readSymbol());
        case 'f':
        {
            Symbol sym = readSymbol();
            return FALSE_SYMBOL.equals(sym) ? false : symbols.intern(sym);
        }
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
            return symbols.intern(readSymbol());
        case 'n':
        {
            Symbol sym = readSymbol();
            return NIL_SYMBOL.equals(sym) ? Token.NIL : symbols.intern(sym);
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
            return TRUE_SYMBOL.equals(sym) ? true : symbols.intern(sym);
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
            return symbols.intern(readSymbol());
        case '+':
        case '-':
            if (isDigit(peek)) {
                return readNumber();
            } else {
                return symbols.intern(readSymbol());
            }
        case ':':
            return keywords.intern(readKeyword());
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
            switch(peek) {
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
            return strings.intern(readStringLiteral());
        case '\\':
            return readCharacterLiteral();
        default:
            throw new EdnException(
                    String.format("Unexpected character '%c', \\"+"u%04x", curr, (int)curr));
        }
    }
    
    private void skipWhitespaceAndComments() throws IOException {
        skipWhitespace();
        while (curr == ';') {
            skipComment();
            skipWhitespace();
        }
    }

    private void skipWhitespace() throws IOException {
        while (isWhitespace(curr) && curr != END) {
            nextChar();
        }
    }

    private void skipComment() throws IOException {
        assert curr == ';';
        do {
            nextChar();
        } while (!isEndOfLine(curr) && curr != END);
    }

    private static final boolean isEndOfLine(char c) {
        return c == '\n' || c == '\r';
    }



    private char readCharacterLiteral() throws IOException {
        assert curr == '\\';
        nextChar();
        if (isWhitespace(curr)) {
            throw new EdnException(
                    "A backslash introducing character literal must not be "+
                    "immediately followed by whitespace.");
        }
        StringBuilder b = new StringBuilder();
        do {
            b.append(curr);
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
        assert curr == '"';
        nextChar();
        StringBuffer b = new StringBuffer();
        while (curr != '"' && curr != END) {
            if (curr == '\\') {
                nextChar();
                switch(curr) {
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
                    throw new EdnException("Unsupported '"+ curr +"' escape in string");
                }
            } else {
                b.append(curr);
            }
            nextChar();
        }
        if (curr == '"') {
            nextChar();
        } else {
            throw new EdnException("Unclosed string literal");
        }
        return b.toString();
    }

    private Number readNumber() throws IOException {
        assert CharClassify.startsNumber(curr);
        StringBuffer digits = new StringBuffer();
        
        if (curr != '+') {
            digits.append(curr);
        }
        while (isDigit(nextChar())) {
            digits.append(curr);
        }

        if (curr == '.' || curr == 'e' || curr == 'E' || curr == 'M') {
            return parseFloat(digits);
        } else {
            return parseInteger(digits);
        }
    }

    private Number parseFloat(StringBuffer digits) throws IOException {
        if (curr == '.') {
            do {
                digits.append(curr);
            } while (isDigit(nextChar()));
        }

        if (curr == 'e' || curr == 'E') {
            digits.append(curr);
            nextChar();
            if (!(curr == '-' || curr == '+' || isDigit(curr))) {
                throw new EdnException("Not a number: '"+ digits + curr +"'.");
            }
            do {
                digits.append(curr);
            } while (isDigit(nextChar()));
        }

        final boolean decimal;
        if (curr == 'M') {
            decimal = true;
            nextChar();
        } else {
            decimal = false;
        }

        if (!separatesTokens(curr)) {
            throw new EdnException("Not a number: '"+ digits + curr +"'.");
        }

        if (decimal) {
            return new BigDecimal(digits.toString());
        } else {
            return Double.parseDouble(digits.toString());
        }
    }

    private Number parseInteger(CharSequence digits) throws IOException {

        final boolean bigint;
        if (curr == 'N') {
            bigint = true;
            nextChar();
        } else {
            bigint = false;
        }

        if (!separatesTokens(curr)) {
            throw new EdnException("Not a number: '"+ digits + curr +"'.");
        }

        final BigInteger n = new BigInteger(digits.toString());

        if (bigint) {
            return n;
        } else {
            if (MIN_INTEGER.compareTo(n) <= 0 && n.compareTo(MAX_INTEGER) <= 0) {
                return Integer.valueOf(n.intValue());
            } else if (MIN_LONG.compareTo(n) <= 0 && n.compareTo(MAX_LONG) <= 0) {
                return Long.valueOf(n.longValue());
            } else {
                return n;
            }
        }
    }

    private Keyword readKeyword() throws IOException {
        assert curr == ':';
        nextChar();
        Symbol sym = readSymbol();
        if (SLASH_SYMBOL.equals(sym)) {
            throw new EdnException("':/' is not a valid keyword.");
        }
        return new Keyword(sym);
    }

    private Tag readTag() throws IOException {
        assert curr == '#';
        nextChar();
        return new Tag(readSymbol());
    }


    private Symbol readSymbol() throws IOException {
        assert CharClassify.symbolStart(curr);

        StringBuilder b = new StringBuilder();
        int n = 0;
        int p = Integer.MIN_VALUE;
        do {
          if (curr == '/') {
              n += 1;
              p = b.length();
          }
          b.append(curr);
          nextChar();
        } while (!separatesTokens(curr));

        validateUseOfSlash(b, n, p);
        return makeSymbol(b, n, p);
    }

    private Symbol makeSymbol(StringBuilder b, int slashCount, int slashPos) {
        if (slashCount == 0) {
            return new Symbol(null, b.toString());
        } else {
            assert slashCount == 1;
            if (slashPos == 0) {
                assert b.length() == 1 && b.charAt(0) == '/';
                return new Symbol(null, b.toString());
            } else {
                return new Symbol(b.substring(0, slashPos), b.substring(slashPos+1));
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
    private static final BigInteger MIN_INTEGER = BigInteger.valueOf(Integer.MIN_VALUE);
    private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);



}
