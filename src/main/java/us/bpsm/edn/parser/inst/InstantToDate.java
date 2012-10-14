// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser.inst;


public final class InstantToDate extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeDate(pi);
    }

}
