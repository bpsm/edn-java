package us.bpsm.edn.parser;

/**
 * Factory for creating {@link Scanner}s.
 */
public class Scanners {

    private static final Scanner DEFAULT_SCANNER =
        new ScannerImpl(Parsers.defaultConfiguration());

    /**
     * Provides a {@link Scanner}.
     *
     * @return a {@link Scanner}, never null.
     */
    public static Scanner newScanner() {
        return DEFAULT_SCANNER;
    }

    private Scanners() {
        throw new UnsupportedOperationException();
    }

}
