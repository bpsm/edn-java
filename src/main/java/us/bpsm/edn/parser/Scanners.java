package us.bpsm.edn.parser;

import java.util.Iterator;

public class Scanners {

    private static final Scanner DEFAULT_SCANNER =
        new ScannerImpl(Parsers.defaultConfiguration());

    public static Scanner newScanner() {
        return DEFAULT_SCANNER;
    }

}
