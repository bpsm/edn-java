// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.examples;

import static bpsm.edn.parser.Parsers.newPushbackReader;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Test;
import bpsm.edn.parser.CollectionBuilder;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;

public class SimpleParserConfigTest {
    @Test
    public void test() throws IOException {
        Parser.Config cfg =
            Parsers.newParserConfigBuilder().setSetFactory(
                    new CollectionBuilder.Factory() {
                public CollectionBuilder builder() {
                    return new CollectionBuilder() {
                        SortedSet<Object> s = new TreeSet<Object>();
                        public void add(Object o) { s.add(o); }
                        public Object build() { return s; }
                    };
                }
            }).build();
        PushbackReader pbr = newPushbackReader("#{1 0 2 9 3 8 4 7 5 6}");
        Parser p = Parsers.newParser(cfg);
        SortedSet<?> s = (SortedSet<?>) p.nextValue(pbr);
        // The elements of s are sorted since our SetFactory
        // builds a SortedSet, not a (Hash)Set.
        assertEquals(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L),
            new ArrayList<Object>(s));
    }
}
