package bpsm.edn.printer;

import java.io.IOException;

import bpsm.edn.EdnException;
import bpsm.edn.protocols.Function;
import bpsm.edn.util.CharClassify;

public final class StringPrinter implements Printer {
	
	private final Config config;
	private final StringBuilder builder=new StringBuilder();
    private int softspace = 0;
	
	public StringPrinter() {
		this(Printers.defaultPrinterConfig());
	}
	
	public StringPrinter(Config config) {
		this.config=config;
	}
	
	@SuppressWarnings("resource")
	public static String printString(Object ednValue) {
		return new StringPrinter().printValue(ednValue).toString();
	}

	public void close() {
		// do nothing
	}

	public Printer printValue(Object ednValue)  {
        Function printFn = config.getPrintFn(ednValue);
        if (printFn == null) {
            throw new EdnException(String.format(
                    "Don't know how to write '%s' of type '%s'",
                    ednValue, Util.getClassOrNull(ednValue)));
        }
        printFn.eval(ednValue, this);
        return this;
    }

	public Printer append(CharSequence csq)  {
        if (softspace > 1 && csq.length() > 0 && !CharClassify.isWhitespace(csq.charAt(0))) {
            builder.append(' ');
        }
        softspace = 0;
		builder.append(csq);
		return this;
	}

	public Printer append(char c)  {
        if (softspace > 1 && !CharClassify.isWhitespace(c)) {
            builder.append(' ');
        }
        softspace = 0;
		builder.append(c);
		return this;
	}

	public Printer append(CharSequence csq, int start, int end)
			throws IOException {
        if (softspace > 1 && csq.length() > 0 && !CharClassify.isWhitespace(csq.charAt(0))) {
            builder.append(' ');
        }
        softspace = 0;
		builder.append(csq.subSequence(start, end));
		return this;
	}

	public Printer softspace() {
        softspace += 1;
        return this;
	}
	
	public String toString() {
		return builder.toString();
	}
	
	public void clear() {
		builder.setLength(0);
	}

}
