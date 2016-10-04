package us.bpsm.edn.examples;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.printer.Printers;

import java.util.Arrays;
import java.util.List;

public class PrintingExamples {
    @Test
    public void printCompactly() {
        Assert.assertThat(ACCEPTABLE_COMPACT_RENDERINGS,
                CoreMatchers.hasItem(Printers.printString(
                        Printers.defaultPrinterProtocol(),
                        VALUE_TO_PRINT)));
    }

    @Test
    public void printPretty() {
        Assert.assertThat(ACCEPTABLE_PRETTY_RENDERINGS,
                CoreMatchers.hasItem(Printers.printString(
                        Printers.prettyPrinterProtocol(),
                        VALUE_TO_PRINT)));
    }

    static final Object VALUE_TO_PRINT;
    static {
        Parser parser = Parsers.newParser(Parsers.defaultConfiguration());
        VALUE_TO_PRINT = parser.nextValue(Parsers.newParseable(
                "{:a [1 2 3],\n" +
                " [x/y] 3.14159}\n"));
    }

    static final List<String> ACCEPTABLE_COMPACT_RENDERINGS = Arrays.asList(
            "{:a[1 2 3][x/y]3.14159}",
            "{[x/y]3.14159 :a[1 2 3]}"
    );

    static final List<String> ACCEPTABLE_PRETTY_RENDERINGS = Arrays.asList(
            "{"           + "\n" +
            "  :a ["      + "\n" +
            "    1"       + "\n" +
            "    2"       + "\n" +
            "    3"       + "\n" +
            "  ]"         + "\n" +
            "  ["         + "\n" +
            "    x/y"     + "\n" +
            "  ] 3.14159" + "\n" +
            "}",
            "{"           + "\n" +
            "  ["         + "\n" +
            "    x/y"     + "\n" +
            "  ] 3.14159" + "\n" +
            "  :a ["      + "\n" +
            "    1"       + "\n" +
            "    2"       + "\n" +
            "    3"       + "\n" +
            "  ]"         + "\n" +
            "}"
    );
}
