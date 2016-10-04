// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class DefaultMapFactory implements CollectionBuilder.Factory {
    @Override
	public CollectionBuilder builder() {
        return new CollectionBuilder() {
            final Object none = new Object();
            final Map<Object,Object> map = new HashMap<Object,Object>();
            Object key = none;
            @Override
			public void add(Object o) {
                if (key == none) {
                    key = o;
                } else {
                    map.put(key, o);
                    key = none;
                }
            }
            @Override
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