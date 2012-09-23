// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser.util;

import java.util.BitSet;

public class CharClassify {

    public static boolean symbolConstituent(char c) {
        return SYMBOL_CONSTITUENTS.get(c);
    }

    public static boolean symbolStart(char c) {
        return SYMBOL_START.get(c);
    }

    public static boolean isDigit(char c) {
        return DIGIT.get(c);
    }

    public static boolean isWhitespace(char c) {
        return WHITESPACE.get(c);
    }

    public static boolean separatesTokens(char c) {
        return TOKEN_SEPARATORS.get(c);
    }

    public static boolean startsNumber(char c) {
        return NUMBER_START.get(c);
    }

    private static final BitSet WHITESPACE;
    static {
        WHITESPACE = new BitSet(128);
        WHITESPACE.set(0, ' '+1);
        WHITESPACE.set(',');
    }

    private static final BitSet DIGIT;
    static {
        DIGIT = new BitSet(128);
        DIGIT.set('0', '9'+1);
    }

    private static final BitSet LETTER;
    static {
        LETTER = new BitSet(128);
        LETTER.set('A', 'Z'+1);
        LETTER.set('a', 'z'+1);
    }

    private static final BitSet NUMBER_START;
    static {
        NUMBER_START = new BitSet(128);
        NUMBER_START.or(DIGIT);
        NUMBER_START.set('-');
        NUMBER_START.set('+');
    }

    private static final BitSet SYMBOL_START;
    static {
        SYMBOL_START = new BitSet(128);
        SYMBOL_START.or(LETTER);
        for (char c: "!*+-./?_".toCharArray()) {
            SYMBOL_START.set(c);
        }
    }

    private static final BitSet SYMBOL_CONSTITUENTS;
    static {
        SYMBOL_CONSTITUENTS = new BitSet(128);
        SYMBOL_CONSTITUENTS.or(SYMBOL_START);
        SYMBOL_CONSTITUENTS.or(DIGIT);
        SYMBOL_CONSTITUENTS.set('#');
        SYMBOL_CONSTITUENTS.set(':');
    }

    private static final BitSet TOKEN_SEPARATORS;
    static {
        TOKEN_SEPARATORS = new BitSet(128);
        TOKEN_SEPARATORS.or(WHITESPACE);
        for (char c: "\"#();[\\]{}".toCharArray()) {
            TOKEN_SEPARATORS.set(c);
        }
    }

}
