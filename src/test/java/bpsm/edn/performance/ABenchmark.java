package bpsm.edn.performance;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

abstract class ABenchmark  extends SimpleBenchmark implements Runnable {
    
    public void run() {
        new Runner().run(new String[] { this.getClass().getCanonicalName(),
            "--timeUnit", "ms"});
    }

    static void parse(int reps, String resourceName) {
        parse(reps, resourceName, true);
    }
    
    static void parse(int reps, String resourceName, boolean doparse) {
        for (int i = 0; i < reps; i++) {
            try {
                Reader r = new InputStreamReader(
                    IndividualBenchmarks.class.getResourceAsStream(resourceName),
                    "UTF-8");
                Parser p = Parsers.newParser(Parsers.defaultConfiguration(), r);
                try {
                    if (doparse) {
                        while (p.nextValue() != Parser.END_OF_INPUT) {
                        }
                    }
                } finally {
                    p.close();
                }
            } catch (UnsupportedEncodingException e) {
                throw new Error(
                    "This Java claims UTF-8 is unsupported, which is in violation of JLS.");
            }
        }
    }
}
