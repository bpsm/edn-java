// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser.inst;

public class InstantToTimestamp extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeTimestamp(pi);
    }

}
