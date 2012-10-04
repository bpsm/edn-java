// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.printer;

import bpsm.edn.protocols.Function;

public interface Printer {

    Printer printValue(Object ednValue);
    Printer append(CharSequence csq);
    Printer append(char c);
    Printer softspace();

    public interface Config {

        Function getPrintFn(Object ednValue);

        public interface Builder {
            @SuppressWarnings("rawtypes")
            Builder bind(Class ednValueClass, Function printFn);

            Config build();
        }

    }
}
