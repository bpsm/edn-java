package us.bpsm.edn.performance;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import us.bpsm.edn.parser.Parseable;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.parser.Parsers;


import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

abstract class ABenchmark  extends SimpleBenchmark implements Runnable {

    @Override
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
                Parseable r = Parsers.newParseable(new InputStreamReader(
                    IndividualBenchmarks.class.getResourceAsStream(resourceName),
                    "UTF-8"));
                Parser p = Parsers.newParser(Parsers.defaultConfiguration());
                try {
                    if (doparse) {
                        while (p.nextValue(r) != Parser.END_OF_INPUT) {
                        }
                    }
                } finally {
                    try {
                        r.close();
                    } catch (IOException e) {
                        // ignore e
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new Error(
                    "This Java claims UTF-8 is unsupported, which is in violation of JLS.");
            }
        }
    }
}
