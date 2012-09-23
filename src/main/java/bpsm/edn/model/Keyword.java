// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.model;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;

import bpsm.edn.parser.util.Interner;

public final class Keyword implements Named, Comparable<Keyword> {
    private final Symbol sym;

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
    }

    public String toString() {
        return ":" + sym.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sym == null) ? 0 : sym.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Keyword other = (Keyword) obj;
        if (sym == null) {
            if (other.sym != null) {
                return false;
            }
        } else if (!sym.equals(other.sym)) {
            return false;
        }
        return true;
    }

    public int compareTo(Keyword o) {
        return sym.compareTo(o.sym);
    }

    private static final Interner<Symbol, Keyword> INTERNER =
            new Interner<Symbol, Keyword>();

}
