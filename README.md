# edn-java

*edn-java* is a library to parse (read) and print (write) [edn](https://github.com/edn-format/edn).

This is very early days. There are still rough edges in the design which may see me moving things around some before I'm happy with the result.

## Installation

This is a Maven project with the following coordinates:

```xml
<dependency>
    <groupId>info.bsmithmannschott</groupId>
    <artifactId>edn-java</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

You'll have to build it form source yourself using Maven.  This library is not currently available in any public repository.

## Parsing

You'll need to create a Parser and supply it with some input. Factory methods are provided which accept either a `java.lang.CharSequence` or a `java.io.Reader`. You can then call `nextValue()` on the Parser to read values form the input. When the input is exhausted, `nextValue()` will return `Parser.END_OF_INPUT`.


```java
package bpsm.edn.examples;

import static bpsm.edn.Keyword.newKeyword;
import static bpsm.edn.Symbol.newSymbol;
import static bpsm.edn.parser.Parsers.defaultConfiguration;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.PushbackReader;
import java.util.Map;
import org.junit.Test;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;

public class ParseASingleMapTest {
    @Test
    public void simpleUsageExample() throws IOException {
        PushbackReader pbr = Parsers.newPushbackReader("{:x 1, :y 2}");
        Parser p = Parsers.newParser(defaultConfiguration());
        Map<?, ?> m = (Map<?, ?>) p.nextValue(pbr);
        assertEquals(m.get(newKeyword(newSymbol(null, "x"))), 1L);
        assertEquals(m.get(newKeyword(newSymbol(null, "y"))), 2L);
        assertEquals(Parser.END_OF_INPUT, p.nextValue(pbr));
    }
}
```

### Mapping from EDN to Java

Most *edn* values map to regular Java types, except in such cases where Java doesn't provide something suitable. Implementations of the types peculiar to edn are provided by the package `bpsm.edn`.

`Symbol` and `Keyword` have an optional `prefix` and a mandatory `name`. Both implement the interface `Named`.

Integers map to, `Long` or `BigInteger` depending on the magnitude of the number. Appending `N` to an integer literal maps to `BigInteger` irrespective of the magnitude.

Floating point numbers with the suffix `M` are  mapeped to `BigDecimal`. All others are mapped to `Double`.

Characters are mapped to `Character`, booleans to `Boolean` and strings to `String`. No great shock there, I trust.

Lists "(...)" and vectors "[...]" are both mapped to implementations of `java.util.List`. A vector maps to a List implementation that also implements the marker interface `java.util.RandomAccess`.

Maps map to `java.util.HashMap` and sets to `java.util.HashSet`.

The parser is provided a a configuration when created:

    Parsers.newParser(Parsers.defaultConfiguration())

The parser can be customized to use different collection classes by first building the appropriate `Parser.Config`:

```java
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
```

### Tagged Values

By default, handlers are provided automatically for `#inst` and `#uuid`, which return a `java.util.Date` and a `java.util.UUID` respectively. Tagged values with an unrecognized tag are mapped to `bpsm.edn.TaggedValue`.

#### Customizing the parsing of instants

The package `bpsm.edn.parser.inst` makes three handlers for `#inst` available:

 - `InstantToDate` is the default and converts each `#inst` to a `java.util.Date`.
 - `InstantToCalendar` converts each `#inst` to a `java.util.Calendar`, which preserves the original GTM offset.
 - `InstantToTimestamp` converts each `#inst` to a `java.sql.Timstamp`, which presrves nanoseconds.

Extend `AbstractInstantHandler` to provide your own implementation of `#inst`.

#### Adding support for your own tags

Use custom handlers may by building an appropriate `Parser.Config`:

```java
package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.PushbackReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import bpsm.edn.Symbol;
import bpsm.edn.Tag;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;
import bpsm.edn.parser.TagHandler;

public class CustomTagHandler {
    @Test
    public void test() throws IOException, URISyntaxException {
        Parser.Config cfg =
            Parsers.newParserConfigBuilder()
                .putTagHandler(Tag.newTag(Symbol.newSymbol("bpsm", "uri")),
                    new TagHandler() {
                        public Object transform(Tag tag, Object value) {
                            return URI.create((String) value);
                        }
                    }).build();
        Parser p = Parsers.newParser(cfg);
        PushbackReader pbr = Parsers.newPushbackReader(
                "#bpsm/uri \"http://example.com\"");
        assertEquals(new URI("http://example.com"), p.nextValue(pbr));
    }
}
```

#### Using pseudo-tags to influence the parsing of numbers

By default, integers not marked as arbitrary precision by the suffix "N" will parse as `java.lang.Long`. This can be influenced by installing handlers for the tag named by the constant `Parser.Config.LONG_TAG`.

```java
package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.io.PushbackReader;
import java.math.BigInteger;
import org.junit.Test;
import bpsm.edn.Tag;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Parsers;
import bpsm.edn.parser.TagHandler;

public class CustomLongHandler {
    @Test
    public void test() throws IOException {
        Parser.Config cfg =
            Parsers.newParserConfigBuilder()
                .putTagHandler(Parser.Config.LONG_TAG, new TagHandler() {
                    public Object transform(Tag tag, Object value) {
                        long n = (Long) value;
                        if (Integer.MIN_VALUE <= n && n <= Integer.MAX_VALUE) {
                            return Integer.valueOf((int) n);
                        } else {
                            return BigInteger.valueOf(n);
                        }
                    }
                }).build();
        Parser p = Parsers.newParser(cfg);
        PushbackReader pbr = Parsers.newPushbackReader("1024, 2147483648");
        assertEquals(1024, p.nextValue(pbr));
        assertEquals(BigInteger.valueOf(2147483648L), p.nextValue(pbr));
    }
}
```

`Parser` also provides `BIG_DECIMAL_TAG`, `DOUBLE_TAG` and `BIG_INTEGER_TAG` to cover customizing all varieties of numbers.

## Printing

The package `bpsm.edn.printer` provides an extensible printer for converting java data structures to valid *edn* text. The default configuration can print values of the following types, as well as Java's `null`, which prints as `nil`:

 - `bpsm.edn.Keyword`
 - `bpsm.edn.Symbol`
 - `bpsm.edn.TaggedValue`
 - `java.lang.Boolean`
 - `java.lang.Byte`
 - `java.lang.CharSequence`, which includes `java.lang.String`.
 - `java.lang.Character`
 - `java.lang.Double`
 - `java.lang.Float`
 - `java.lang.Integer`
 - `java.lang.Long`
 - `java.lang.Short`
 - `java.math.BigInteger`
 - `java.meth.BigDecimal`
 - `java.sql.Timestamp`, as `#inst`.
 - `java.util.Date`, as `#inst`.
 - `java.util.GregorianCalendar`, as `#inst`.
 - `java.util.List`, as `[...]` or `(...)`.
 - `java.util.Map`
 - `java.util.Set`
 - `java.util.UUID`, as `#uuid`.

The `Printer` writes *characters* to the underlying `Writer`. To serialize this text to a file or across a network you'll need to arrange to convert the characters to bytes. Use *UTF-8*, as *edn* specifies.

### Supporting additional types

To support additional types, you'll need to provide a `Printer.Config` to the `Printer` which binds your custom `PrintFn` to the class (or interface) it is responsible for.

As an example, we'll add printing support for URIs:

```java
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
```

### Limitations

 - Edn values must be *acyclic*. Any attempt to print a data structure containing cycles will surely end in a stack overflow.
 - Currently, the printer doesn't pretty-print. This is fine when using edn for communication, but less than helpful for debugging or storage in version control.

