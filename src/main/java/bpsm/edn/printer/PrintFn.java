package bpsm.edn.printer;

import java.io.IOException;

import bpsm.edn.protocols.Function;

public abstract class PrintFn<E> implements Function {

    @SuppressWarnings("unchecked")
    public final Object eval(Object self, Object argument) {
        try {
            eval((E)self, (Printer)argument);
        } catch (IOException e) {
            throw new PrintException(e);
        }
        return null;
    }
    
    protected abstract void eval(E self, Printer writer) throws IOException;

}
