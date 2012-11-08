// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.printer;


public interface Printer {

    Printer printValue(Object ednValue);
    Printer append(CharSequence csq);
    Printer append(char c);
    Printer softspace();
    void close();

    public interface Fn<E> {
        /**
         * Implementations which may generate an {@code IOException} should
         * throw it by first wrapping it in an {@code EdnIOException}.
         * @param self some edn value to be written
         * @param printer the printer that called us, giving us access to
         *   its append methods which are required to actually append
         *   characters to the output being generated.
         */
        public abstract void eval(E self, Printer printer);
    }
}
