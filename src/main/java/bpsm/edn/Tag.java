// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn;


public final class Tag implements Named, Comparable<Tag> {
    private final Symbol sym;

    public final String getPrefix() {
        return sym.getPrefix();
    }

    public final String getName() {
        return sym.getName();
    }

    public static Tag newTag(Symbol sym) {
        return new Tag(sym);
    }
    
    private Tag(Symbol sym) {
        if (sym == null) {
            throw new NullPointerException();
        }
        this.sym = sym;
    }

    public String toString() {
        return "#" + sym.toString();
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
        Tag other = (Tag) obj;
        if (sym == null) {
            if (other.sym != null) {
                return false;
            }
        } else if (!sym.equals(other.sym)) {
            return false;
        }
        return true;
    }

    public int compareTo(Tag o) {
        return sym.compareTo(o.sym);
    }



}
