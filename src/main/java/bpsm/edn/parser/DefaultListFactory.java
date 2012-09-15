package bpsm.edn.parser;

import java.util.LinkedList;

final class DefaultListFactory implements BuilderFactory {
    public CollectionBuilder builder() {
        return new CollectionBuilder() {
            LinkedList<Object> list = new LinkedList<Object>();
            public void add(Object o) {
                list.add(o);
            }
            public Object build() {
                return list;
            }
        };
    }
}