// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import us.bpsm.edn.Tag;

/**
 * When a {@link Parser} encounters {@code #tag someEdnValue}, it uses
 * the  TagHandler registered  for  {@code #tag}  to transform  {@code
 * someEdnValue} before including it in the results of the parse.
 */
public interface TagHandler {

    /**
     * Consume {@code originalValue}, which is some edn value,
     * returning the value to replace it.
     *
     * @param tag the tag which preceded value, never null.
     * @param originalValue as parsed from the input, may be null.

     * @return a value to be used by the parser as the replacement for
     * {@code originalValue}.
     */
    Object transform(Tag tag, Object originalValue);

}
