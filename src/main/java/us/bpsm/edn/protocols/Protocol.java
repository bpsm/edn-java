// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.protocols;

public interface Protocol<F> {
    String name();

    @SuppressWarnings("rawtypes")
    F lookup(Class selfClass);

    public interface Builder<F> {
        @SuppressWarnings("rawtypes")
        Builder<F> put(Class selfClass, F fn);

        Protocol<F> build();
    }
}
