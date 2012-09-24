// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.model;

import bpsm.edn.parser.util.Interner;

public final class Keyword implements Named, Comparable<Keyword> {
    private final Symbol sym;
    private final int hash;

    public final String getPrefix() {
        return sym.getPrefix();
    }

    public final String getName() {
        return sym.getName();
    }
    
    public static Keyword newKeyword(Symbol sym) {
        return INTERNER.intern(sym, new Keyword(sym));
    }

    private Keyword(Symbol sym) {
        if (sym == null) {
            throw new NullPointerException();
        }
        this.sym = sym;
        hash = sym.hashCode() + 0x9e3779b9;
    }

    public String toString() {
        return ":" + sym.toString();
    }

    public int compareTo(Keyword o) {
    	if (this==o) return 0;
        return sym.compareTo(o.sym);
    }

    private static final Interner<Symbol, Keyword> INTERNER =
            new Interner<Symbol, Keyword>();

    @Override
    public boolean equals(Object o) {
    	// we can compare based on object identity, because keywords are globally interned
    	return (this==o);
    }
    
    @Override
    public int hashCode() {
    	return hash;
    }
}
