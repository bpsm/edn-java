package us.bpsm.edn.examples;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import org.junit.Test;

import us.bpsm.edn.Tag;
import us.bpsm.edn.printer.Printer;
import us.bpsm.edn.printer.Printer.Fn;
import us.bpsm.edn.printer.Printers;
import us.bpsm.edn.protocols.Protocol;

public class CustomTagPrinter {
    private static final Tag BPSM_URI = Tag.newTag("us.bpsm", "uri");
    @Test
    public void test() throws IOException {
        Protocol<Fn<?>> fns = Printers.defaultProtocolBuilder()
                .put(URI.class, new Printer.Fn<URI>() {
                    @Override
                    public void eval(URI self, Printer writer) {
                        writer.printValue(BPSM_URI).printValue(self.toString());
                    }})
                    .build();
        StringWriter w = new StringWriter();
        Printer p = Printers.newPrinter(fns, w);
        p.printValue(URI.create("http://example.com"));
        p.close();
        assertEquals("#us.bpsm/uri\"http://example.com\"", w.toString());
    }
}
