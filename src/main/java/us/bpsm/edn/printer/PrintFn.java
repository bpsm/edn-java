// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.printer;

import us.bpsm.edn.protocols.Function;

public abstract class PrintFn<E> implements Function {

    @SuppressWarnings("unchecked")
    public final Object eval(Object self, Object argument) {
        eval((E)self, (Printer)argument);
        return null;
    }

    /**
     * Implementations which may generate an {@code IOException} should
     * throw it by first wrapping it in an {@code EdnIOException}.
     * @param self some edn value to be written
     * @param printer the printer that called us, giving us access to
     *   its append methods which are required to actually append
     *   characters to the output being generated.
     */
    protected abstract void eval(E self, Printer printer);

}
