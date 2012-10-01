package bpsm.edn.parser.inst;

public class InstantToCalendar extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeCalendar(pi);
    }

}
