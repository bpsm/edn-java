// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser.input;

public class CharSequenceInput implements Input {
    CharSequence cs;
    int i;

    public CharSequenceInput(CharSequence cs) {
        this.cs = cs;
        this.i = 0;
    }

    public char next() {
        return i < cs.length() ? cs.charAt(i++) : 0;
    }
}
