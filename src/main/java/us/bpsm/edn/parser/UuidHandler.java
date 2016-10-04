// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import java.util.UUID;

import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;


class UuidHandler implements TagHandler {

    @Override
	public Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
             throw new EdnSyntaxException(tag.toString() +
                                          " expectes a String.");
        }
        return UUID.fromString((String) value);
    }

}
