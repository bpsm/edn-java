package us.bpsm.edn.examples;

import org.junit.Assert;
import org.junit.Test;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.printer.Printers;

public class PrintingExamples {
    @Test
    public void printCompactly() {
        Assert.assertEquals(
                EXPECTED_COMPACT_RENDERING,
                Printers.printString(
                        Printers.defaultPrinterProtocol(), VALUE_TO_PRINT));
    }

    @Test
    public void printPretty() {
        Assert.assertEquals(
                EXPECTED_PRETTY_RENDERING,
                Printers.printString(
                        Printers.prettyPrinterProtocol(), VALUE_TO_PRINT));
    }

    static final Object VALUE_TO_PRINT;
    static {
        Parser parser = Parsers.newParser(Parsers.defaultConfiguration());
        VALUE_TO_PRINT = parser.nextValue(Parsers.newParseable(
                "{:a [1 2 3],\n" +
                " [x/y] 3.14159}\n"));
    }

    static final String EXPECTED_COMPACT_RENDERING =
            "{:a[1 2 3][x/y]3.14159}";

    static final String EXPECTED_PRETTY_RENDERING =
            "{"           + "\n" +
            "  :a ["      + "\n" +
            "    1"       + "\n" +
            "    2"       + "\n" +
            "    3"       + "\n" +
            "  ]"         + "\n" +
            "  ["         + "\n" +
            "    x/y"     + "\n" +
            "  ] 3.14159" + "\n" +
            "}";
}
