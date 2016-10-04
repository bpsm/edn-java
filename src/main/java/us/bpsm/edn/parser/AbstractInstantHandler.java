// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;

/**
 * This class may be extended to support additional {@code #inst}
 * representations.
 *
 * @see InstantToCalendar
 * @see InstantToDate
 * @see InstantToTimestamp
 * @see ParsedInstant
 */
public abstract class AbstractInstantHandler implements TagHandler {

    @Override
	public final Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
            throw new EdnSyntaxException(tag.toString() + " expects a String.");
        }
        return transform(InstantUtils.parse((String) value));
    }

    /**
     * This will be called by the Parser when parsing an {@code #inst} value.
     * Implement it to return an instance of your chosen instant representation.
     *
     * @param pi
     *            The contents fields of string following the {@code #inst} tag
     *            as a {@link ParsedInstant}.
     * @return Some value representing the instant pi. Never null.
     */
    protected abstract Object transform(ParsedInstant pi);

}
