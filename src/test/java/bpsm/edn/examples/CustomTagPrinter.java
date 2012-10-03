package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import org.junit.Test;
import bpsm.edn.Symbol;
import bpsm.edn.Tag;
import bpsm.edn.printer.PrintFn;
import bpsm.edn.printer.Printer;
import bpsm.edn.printer.Printers;

public class CustomTagPrinter {
    private static final Tag BPSM_URI = 
        Tag.newTag(Symbol.newSymbol("bpsm", "uri"));
    @Test
    public void test() throws IOException {
        StringWriter w = new StringWriter();
        Printer.Config cfg = Printers.newPrinterConfigBuilder()
            .bind(URI.class, new PrintFn<URI>() {
                @Override
                protected void eval(URI self, Printer writer) {
                    writer.printValue(BPSM_URI).printValue(self.toString());
                }})
            .build();
        Printer p = Printers.newPrinter(cfg, w);
        p.printValue(URI.create("http://example.com"));
        p.close();
        assertEquals("#bpsm/uri\"http://example.com\"", w.toString());
    }
}
