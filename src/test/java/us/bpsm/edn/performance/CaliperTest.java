package us.bpsm.edn.performance;

import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;
import us.bpsm.edn.printer.Printers;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

/**
 * A basic test of parsing and printing speed.
 */
public class CaliperTest extends SimpleBenchmark {

    /**
     * @param reps number of times to parse the test string.
     */
    @SuppressWarnings("unused")
    public void timeStringParsing(int reps) {
        Parseable r = Parsers.newParseable(PerformanceTest.ednString);
        for (int i=0; i<reps; i++) {
            Parser p=Parsers.newParser(Parsers.defaultConfiguration());
            Object o=p.nextValue(r);
        }
    }

    /**
     * @param reps number of times to call print the test data.
     */
    @SuppressWarnings("unused")
    public void timeStringWriting(int reps) {
        for (int i=0; i<reps; i++) {
            String s=Printers.printString(PerformanceTest.ednData);
        }
    }

    /**
     * @param args ignored.
     */
    public static void main(String[] args) {
        new CaliperTest().run();
    }

    private void run() {
        new Runner().run(new String[] {this.getClass().getCanonicalName()});
    }

}
