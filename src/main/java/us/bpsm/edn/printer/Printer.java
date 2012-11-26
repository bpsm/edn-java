// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.printer;

/**
 * A Printer knows how to print edn values in edn syntax to an
 * underlying stream of characters. Use {@link Printers} to create new
 * {@code Printer}s.
 *
 * <p>Printer is mutable, stateful and should only be used form a
 * single thread.
 */
public interface Printer {

    /**
     * Print {@code ednValue} in edn syntax to the underlying
     * character stream.
     *
     * @param ednValue some value to be printed.
     *
     * @return this Printer (for method chaining)
     *
     * @throws EdnException if the printer does not know how to print
     *         values of the type of {@code ednValue}.
     */
    Printer printValue(Object ednValue);

    /**
     * Append the given characters to the underlying character stream.
     *
     * @param csq characters to append to the underlying stream.
     *
     * @return this Printer (for method chaining)
     */
    Printer append(CharSequence csq);

    /**
     * Append the given character to the underlying character stream.
     *
     * @param c character to append to the underlying stream.
     *
     * @return this Printer (for method chaining)
     */
    Printer append(char c);

    /**
     * Increment the internal softspace counter.  Appending via {@code
     * append()} will check this counter and insert a space if the
     * counter is greater than 1 before resetting the counter to zero.
     *
     * <p>This is used by implementations of {@link Fn} to assure that
     * spaces are inserted where they are necessary to enable
     * unambiguous parsing. For example: between two symbols.</p>
     *
     * @return this Printer (for method chaining)
     */
    Printer softspace();

    /**
     * Close the underlying sequence of characters.
     *
     * @throws EdnIOException if the underlying sequence of characters is
     *         {@link java.io.Closeable} and the attempt to close it fails.
     */
    void close();

    public interface Fn<E> {
        /**
         * Implementations which may provoke an
         * {@link java.io.IOException} should wrapping it in an
         * {@link us.bpsm.edn.EdnIOException} before rethrowing it.
         *
         * @param self some edn value to be printed.
         * @param printer the printer that called us, giving us access to
         *   its append methods which are required to actually append
         *   characters to the output being generated.
         */
        public abstract void eval(E self, Printer printer);
    }
}
