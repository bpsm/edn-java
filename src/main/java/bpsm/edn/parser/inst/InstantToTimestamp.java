package bpsm.edn.parser.inst;

public class InstantToTimestamp extends AbstractInstantHandler {

    @Override
    protected Object transform(ParsedInstant pi) {
        return InstantUtils.makeTimestamp(pi);
    }

}
