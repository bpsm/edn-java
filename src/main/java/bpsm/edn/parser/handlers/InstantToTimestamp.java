package bpsm.edn.parser.handlers;

public class InstantToTimestamp extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeTimestamp(pi);
    }

}
