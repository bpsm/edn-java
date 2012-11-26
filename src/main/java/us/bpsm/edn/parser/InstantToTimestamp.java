// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

/**
 * A Handler for {@code #inst} which translates the intant into a
 * {@link java.sql.TimeStamp}.
 */
public class InstantToTimestamp extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeTimestamp(pi);
    }

}
