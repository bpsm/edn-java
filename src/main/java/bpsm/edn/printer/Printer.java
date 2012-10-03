// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.printer;

import bpsm.edn.EdnIOException;
import bpsm.edn.protocols.Function;

public interface Printer {

    Printer printValue(Object ednValue) throws EdnIOException;
    Printer append(CharSequence csq) throws EdnIOException;
    Printer append(char c) throws EdnIOException;
    Printer softspace();
    void close() throws EdnIOException;

    public interface Config {

        Function getPrintFn(Object ednValue);

        public interface Builder {
            @SuppressWarnings("rawtypes")
            Builder bind(Class ednValueClass, Function printFn);

            Config build();
        }

    }
}
