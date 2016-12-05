// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import us.bpsm.edn.EdnSyntaxException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class DefaultMapFactory implements CollectionBuilder.Factory {
    public CollectionBuilder builder() {
        return new CollectionBuilder() {
            final Object none = new Object();
            final Map<Object,Object> map = new HashMap<Object,Object>();
            Object key = none;
            public void add(Object o) {
                if (key == none) {
                    key = o;
                } else {
                    if (map.containsKey(key)) {
                        throw new EdnSyntaxException(String.format("Duplicate map key: %s", key));
                    }
                    map.put(key, o);
                    key = none;
                }
            }
            public Object build() {
                if (key != none) {
                    throw new IllegalStateException(
                            "Every map must have an equal number of keys and values.");
                }
                return Collections.unmodifiableMap(map);
            }
        };
    }
}