package us.bpsm.edn.parser;


public class Scanners {

    private static final Scanner DEFAULT_SCANNER =
        new ScannerImpl(Parsers.defaultConfiguration());

    public static Scanner newScanner() {
        return DEFAULT_SCANNER;
    }

}
