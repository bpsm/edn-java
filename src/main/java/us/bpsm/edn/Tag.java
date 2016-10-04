// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn;

import java.io.Serializable;

import static us.bpsm.edn.Symbol.newSymbol;

/**
 * A Tag is {@linkplain Named}. Additionally it obeys the syntactic restrictions
 * defined for <a href="https://github.com/edn-format/edn#symbols">edn
 * Symbols</a>.
 * <p>
 * Note: Tags print with a leading hash, but this is not part of the tag's name:
 * 
 * <pre>
 * {@code // For the tag "#foo/bar"
 * Tag t = newTag("foo", "bar");
 * t.getName()   => "bar"
 * t.getPrefix() => "foo"
 * t.toString()  => "#foo/bar"}
 * </pre>
 */
public final class Tag implements Named, Comparable<Tag>, Serializable {
    private final Symbol sym;

    /** {@inheritDoc} */
    @Override
	public final String getPrefix() {
        return sym.getPrefix();
    }

    /** {@inheritDoc} */
    @Override
	public final String getName() {
        return sym.getName();
    }

    /**
     * Return a Tag with the same prefix and name as {@code sym}.
     * 
     * @param sym
     *            a Symbol, never null
     * @return a Tag with the same prefix and name as {@code sym}.
     */
    public static Tag newTag(Symbol sym) {
        return new Tag(sym);
    }

    /**
     * Provide a Tag with the given prefix and name.
     * <p>
     * Bear in mind that tags with no prefix are reserved for use by the edn
     * format itself.
     * 
     * @param prefix
     *            An empty String or a non-empty String obeying the restrictions
     *            specified by edn. Never null.
     * @param name
     *            A non-empty string obeying the restrictions specified by edn.
     *            Never null.
     * @return a Keyword, never null.
     */
    public static Tag newTag(String prefix, String name) {
        return newTag(newSymbol(prefix, name));
    }

    /**
     * This is equivalent to {@code newTag("", name)}.
     * 
     * @param name
     *            A non-empty string obeying the restrictions specified by edn.
     *            Never null.
     * @return a Tag without a prefix, never null.
     * @see #newTag(String, String)
     */
    public static Tag newTag(String name) {
        return newTag(newSymbol(EMPTY, name));
    }

    private Tag(Symbol sym) {
        if (sym == null) {
            throw new NullPointerException();
        }
        this.sym = sym;
    }

    @Override
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

    @Override
	public int compareTo(Tag o) {
        return sym.compareTo(o.sym);
    }

}
