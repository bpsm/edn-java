// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.protocols;

public interface Protocol {
    String name();

    @SuppressWarnings("rawtypes")
    Function lookup(Class selfClass);

    public interface Builder {
        @SuppressWarnings("rawtypes")
        Builder put(Class selfClass, Function fn);

        Protocol build();
    }
}
