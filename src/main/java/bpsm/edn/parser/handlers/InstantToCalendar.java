package bpsm.edn.parser.handlers;

public class InstantToCalendar extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeCalendar(pi);
    }

}
