// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import us.bpsm.edn.EdnException;
import us.bpsm.edn.Tag;

public abstract class AbstractInstantHandler implements TagHandler {

    public Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
            throw new EdnException(tag.toString() + " expects a String.");
        }
        return transform(InstantUtils.parse((String)value));
    }

    protected abstract Object transform(ParsedInstant pi);

}
