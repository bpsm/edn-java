// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import bpsm.edn.model.Tag;

public interface TagHandler {

    Object transform(Tag tag, Object value);
    
}
