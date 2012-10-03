// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.printer;

import java.io.IOException;

import bpsm.edn.EdnIOException;
import bpsm.edn.protocols.Function;

public abstract class PrintFn<E> implements Function {

    @SuppressWarnings("unchecked")
    public final Object eval(Object self, Object argument) {
        eval((E)self, (Printer)argument);
        return null;
    }
    
    protected abstract void eval(E self, Printer writer) throws EdnIOException;

}
