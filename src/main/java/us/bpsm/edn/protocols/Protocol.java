// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.protocols;

/**
 * A Protocol is a thread-safe immutable mapping from classes to
 * values of some type {@code F}. Each Protocol is created by a {@link
 * Protocol.Builder}.
 * @param <F> whatever kind of thing you'd like to map to. Generally this will
 * be some class or interface representing a function.
 */
public interface Protocol<F> {

    /**
     * The name of this protocol. (This is intended to support
     * debugging scenarios.)
     * @return The name of this protocol; not null.
     */
    String name();

    /**
     * Return the {@code F} associated with {@code selfClass}.
     *
     * <p>If the Protocol does not contain a mapping for exactly
     * {@code selfClass}, then try {@code selfClass}'s supertypes in
     * <a href="http://en.wikipedia.org/wiki/C3_linearization">C3</a>
     * linearized order, only returning {@code null}
     * when all sueprtypes have been tried without finding a match.
     *
     * <p>{@code selfClass} may be {@code null}, in which case return
     * the {@code F} associated with {@code null}. {@code null} has no
     * supertypes, so if no {@code F} is associated with {@code null},
     * return {@code null}.
     *
     * @param selfClass find an F for this class. May be {@code null}.
     *
     * @return an F, or {@code null}.
     */
    @SuppressWarnings("rawtypes")
    F lookup(Class selfClass);

    /**
     * A single-use builder for Protocol. This builder is not intended
     * for use from more than once thread. Use
     * {@link Protocols#builder(String)} to obtain new instances.
     * @param <F> whatever kind of thing you'd like to map to. Generally this
     *            will be some class or interface representing a function.
     */
    public interface Builder<F> {

        /**
         * Associate the {@code F} {@code fn} with the class {@code
         * selfClass}.
         *
         * @param selfClass represents a class or interface or is
         *        {@code null}.
         *
         * @param fn the value to associate with {@code selfClass}.
         *        {@code fn} must not be {@code null}.
         *
         * @return this Builder (for method chaining).
         *
         * @throws IllegalStateException if {@code build()} was
         *         previously called on this Builder.
         */
        @SuppressWarnings("rawtypes")
        public Builder<F> put(Class selfClass, F fn);

        /**
         * Build and return the {@link Protocol} described by the
         * preceding calls made on this Builder. Calling {@code build()}
         * invalidates this builder.
         *
         *
         * @return a Protocol, not null.
         *
         * @throws IllegalStateException if {@code build()} was
         *         previously called on this Builder.
         */
        public Protocol<F> build();
    }
}
