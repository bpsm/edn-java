package bpsm.edn.printer;

import java.io.Closeable;
import java.io.IOException;

import bpsm.edn.protocols.Function;

public interface Printer extends Closeable, Appendable {

    Printer printValue(Object ednValue) throws IOException;
    Printer append(CharSequence csq) throws IOException;
    Printer append(char c) throws IOException;
    Printer append(CharSequence csq, int start, int end) throws IOException;
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
