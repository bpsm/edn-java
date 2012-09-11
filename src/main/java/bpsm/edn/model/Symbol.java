// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.model;

import static bpsm.edn.parser.util.CharClassify.isDigit;
import static bpsm.edn.parser.util.CharClassify.symbolStart;
import bpsm.edn.parser.EdnException;
import bpsm.edn.parser.util.CharClassify;

public final class Symbol implements Named, Comparable<Symbol> {
    
    final String prefix;
    final String name;
    
    public final String getPrefix() {
        return prefix;
    }

    public final String getName() {
        return name;
    }
     
    
    public Symbol(String prefix, String name) {
        checkArguments(prefix, name);
        this.prefix = prefix;
        this.name = name;
    }
    
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getClass().getName().hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Symbol other = (Symbol) obj;
        if (!name.equals(other.name))
            return false;
        if (prefix == null) {
            if (other.prefix != null)
                return false;
        } else if (!prefix.equals(other.prefix))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (prefix == null)
            return name;
        return prefix + "/" + name;
    }
    
    private static void checkArguments(String prefix, String name) {
        if (name == null) {
            throw new EdnException("name must not be null.");
        }
        checkName("name", name);
        if (prefix != null) {
            checkName("prefix", prefix);
        }
    }
    
    private static void checkName(String label, String ident) {
        if (ident.length() == 0) {
            throw new EdnException("The "+ label +" '"+ ident +"' must not be empty.");
        }
        char first = ident.charAt(0);
        if (isDigit(first)) {
            throw new EdnException("The "+ label +" '"+ ident +"' must not begin with a digit.");
        }
        if (!symbolStart(first)) {
            throw new EdnException("The "+ label +" '"+ ident +"' begins with a forbidden character.");
        }
        if ((first == '.' || first == '-') && ident.length() > 1 && isDigit(ident.charAt(1))) {
            throw new EdnException("The "+ label +" '"+ ident +"' begins with a '-' or '.' followed by digit, which is forbidden.");
        }
        int n = ident.length();
        for (int i = 1; i < n; i++) {
            if (!CharClassify.symbolConstituent(ident.charAt(i))) {
                throw new EdnException("The "+ label +" '"+ ident +"' contains the illegal character '"+ ident.charAt(i) +"' at offset "+ i +".");
            }
        }
    }

    public int compareTo(Symbol right) {
        int cmp = nullToEmpty(this.prefix).compareTo(nullToEmpty(right.prefix));
        if (cmp != 0) {
            return cmp;
        }
        return this.name.compareTo(right.name);
    }
    
    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
    
}
