// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import java.util.UUID;

import bpsm.edn.model.EdnException;
import bpsm.edn.model.Tag;

class UuidHandler implements TagHandler {

    public Object transform(Tag tag, Object value) {
        if (!(value instanceof String)) {
             throw new EdnException(tag.toString() + " expectes a String.");
        }
        return UUID.fromString((String) value);
    }

}
