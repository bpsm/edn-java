// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser.input;

public interface Input {
    public static final char END = 0;
    public char next();
}
