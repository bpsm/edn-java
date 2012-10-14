// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

final class DefaultSetFactory implements CollectionBuilder.Factory {
    public CollectionBuilder builder() {
        return new CollectionBuilder() {
            Set<Object> set = new HashSet<Object>();
            public void add(Object o) {
                set.add(o);
            }
            public Object build() {
                return Collections.unmodifiableSet(set);
            }
        };
    }
}