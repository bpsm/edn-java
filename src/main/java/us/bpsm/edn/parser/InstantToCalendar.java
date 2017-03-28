// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

/**
 * A Handler for {@code #inst} which translates the instant into a
 * {@link java.util.Calendar}.
 */
public class InstantToCalendar extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeCalendar(pi);
    }

}
