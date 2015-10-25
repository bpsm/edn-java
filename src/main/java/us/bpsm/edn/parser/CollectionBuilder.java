// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

/**
 * The parser uses each CollectionBuilder to build a set, map, vector
 * or list.
 */
public interface CollectionBuilder {

    /**
     * Add an item to the collection being built. In the case of a
     * map, this will be called an even number of times, first for a
     * key and then for its corresponding value until all key-value
     * pairs of the map have been added.
     * <p>
     * For other collections is can be called any number of times.
     *
     * <p>{@code add()} may not be called after {@code build()}.
     * @param o an object to add to the collection under construction. o may
     *          be null.
     */
    public void add(Object o);

    /**
     * Return the collection containing all the elements previously
     * added. {@code build()} may only be called once. After {@code
     * build()} has been called, the builder is rendered useless and
     * can be discarded.
     *
     * @return The collection. Generally a Set, Map or some kind of List.
     */
    public Object build();

    /**
     * The parser uses CollectionBuilder.Factory instances to get a
     * fresh CollectionBuilder each time it needs to build a set, map,
     * vector or list. (Any given Factory produces Collection builders
     * for either sets, maps, lists or vectors.)
     */
    public interface Factory {

        /**
         * Returns a new CollectionBuilder.
         * @return a new CollectionBuilder, never null.
         */
        CollectionBuilder builder();
    }
}
