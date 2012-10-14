// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import us.bpsm.edn.Tag;

public interface TagHandler {

    Object transform(Tag tag, Object value);

}
