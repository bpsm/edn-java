// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

public interface CollectionBuilder {
    public void add(Object o);
    public Object build();
    
    public interface Factory {
        CollectionBuilder builder();
    }
}
