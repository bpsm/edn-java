// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn;

/**
 * A named thing has a local {@code name} which may be further qualified by a
 * {@code prefix}. A prefix will always be present (not null), but may be empty.
 */
public interface Named {

    static final String EMPTY = "".intern();

    /**
     * The name of this named thing, not including any prefix.
     * 
     * @return a non-empty string.
     */
    String getName();

    /**
     * The prefix, (also called namespace), which may be empty. A Named object
     * with an empty prefix is said to have no prefix.
     * 
     * @return a possibly empty string; never null.
     */
    String getPrefix();
}
