// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser.util;

import java.util.Arrays;

public class CharClassify {

    public static boolean symbolConstituent(char c) {
        return Arrays.binarySearch(SYMBOL_CONSTITUENTS_SORTED, c) >= 0;
    }

    public static boolean symbolStart(char c) {
        return Arrays.binarySearch(SYMBOL_START_SORTED, c) >= 0;
    }

    public static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public static boolean isWhitespace(char c) {
        return c <= ' ' || c == ',';
    }

    public static boolean separatesTokens(char c) {
        return isWhitespace(c)
            || Arrays.binarySearch(TOKEN_SEPARATORS_SORTED, c) >= 0;
    }

    public static boolean startsNumber(char c) {
        return isDigit(c) || c == '-';
    }


    private static final char[] SYMBOL_START_SORTED =
            "!*+-./?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz"
            .toCharArray();
    static { Arrays.sort(SYMBOL_START_SORTED); }

    private static final char[] SYMBOL_CONSTITUENTS_SORTED =
            "!#*+-./0123456789:?ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz"
            .toCharArray();
    static { Arrays.sort(SYMBOL_CONSTITUENTS_SORTED); }

    private static final char[] TOKEN_SEPARATORS_SORTED =
            "\"#();[\\]{}"
            .toCharArray();
    static { Arrays.sort(TOKEN_SEPARATORS_SORTED); }

}
