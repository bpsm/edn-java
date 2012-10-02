# edn-java

*edn-java* is a library to parse (read) and print (write) [edn](https://github.com/edn-format/edn).

This is very early days. Consider all of this library subject to change, incomplete and probably buggy. There are still rough edges in the design which may see me moving things around some before I'm happy with the result.

## Installation

This is a Maven project with the following coordinates:

    <dependency>
        <groupId>info.bsmithmannschott</groupId>
        <artifactId>edn-java</artifactId>
        <version>0.2-SNAPSHOT</version>
    </dependency>

You'll have to build it form source yourself using Maven.  This library is not currently available in any public repository.

## Parsing

You'll need to create a Parser and supply it with some input. Factory methods are provided which accept either a `CharSequence` or a `java.io.Reader`. You can then call `nextValue()` on the Parser to read values form the input. When the input is exhausted, `nextValue()` will return `Token.END_OF_INPUT`.


```java
package bpsm.edn.examples;

import static bpsm.edn.model.Keyword.newKeyword;
import static bpsm.edn.model.Symbol.newSymbol;
import static bpsm.edn.parser.ParserConfiguration.defaultConfiguration;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import bpsm.edn.parser.Parser;
import bpsm.edn.parser.Token;

public class ParseASingleMapTest {
    @Test
    public void simpleUsageExample() throws IOException {
        Parser p = Parser.newParser(defaultConfiguration(), "{:x 1, :y 2}");
        try {
            Map<?, ?> m = (Map<?, ?>) p.nextValue();
            assertEquals(m.get(newKeyword(newSymbol(null, "x"))), 1L);
            assertEquals(m.get(newKeyword(newSymbol(null, "y"))), 2L);

            assertEquals(Token.END_OF_INPUT, p.nextValue());
        } finally {
            p.close();
        }
    }
}
```

### Mapping from EDN to Java

Most *edn* values map to regular Java types, except in such cases where Java doesn't provide something suitable. Implementations of the types peculiar to edn are provided by the package `bpsm.edn.model`.

`Symbol` and `Keyword` have an optional `prefix` and a mandatory `name`. Both implement the interface `Named`.

Integers map to, `Long` or `BigInteger` depending on the magnitude of the number. Appending `N` to an integer literal maps to `BigInteger` irrespective of the magnitude.

Floating point numbers with the suffix `M` are  mapeped to `BigDecimal`. All others are mapped to `Double`.

Characters are mapped to `Character`, booleans to `Boolean` and strings to `String`. No great shock there, I trust.

Lists "(...)" and vectors "[...]" are both mapped to implementations of `java.util.List`. A vector maps to a List implementation that also implements `java.util.RandomAccess`.

Maps map to `java.util.HashMap` and sets to `java.util.HashSet`.

The parser is customized by providing it with a ParserConfiguration when you create it:

    Parser.newParser(ParserConfiguration.defaultConfiguration(), input)

The parser can be customized to use different collection classes by first building the appropriate `ParserConfiguration`:

```java
package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import bpsm.edn.parser.BuilderFactory;
import bpsm.edn.parser.CollectionBuilder;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.ParserConfiguration;

public class SimpleParserConfigTest {
    @Test
    public void test() throws IOException {
        ParserConfiguration cfg =
            ParserConfiguration.builder().setSetFactory(new BuilderFactory() {
                public CollectionBuilder builder() {
                    return new CollectionBuilder() {
                        SortedSet<Object> s = new TreeSet<Object>();
                        public void add(Object o) { s.add(o); }
                        public Object build() { return s; }
                    };
                }
            }).build();
        Parser p = Parser.newParser(cfg, "#{1 0 2 9 3 8 4 7 5 6}");
        SortedSet<?> s = (SortedSet<?>) p.nextValue();
        // The elements of s are sorted since our SetFactory
        // builds a SortedSet, not a (Hash)Set.
        assertEquals(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L),
            new ArrayList<Object>(s));
    }
}
```

### Tagged Values

By default, handlers are provided automatically for `#inst` and `#uuid`, which return a `java.util.Date` and a `java.util.UUID` respectively. Tagged values with an unrecognized tag are mapped to `bpsm.edn.model.TaggedValue`.

#### Customizing the parsing of instants

The package `bpsm.edn.parser.handlers` makes three handlers for `#inst` available:

 - `InstantToDate` is the default and converts each `#inst` to a `java.util.Date`.
 - `InstantToCalendar` converts each `#inst` to a `java.util.Calendar`, which preserves the original time zone.
 - `InstantToTimestamp` converts each `#inst` to a `java.sql.Timstamp`, which presrves nanoseconds.

Extend `AbstractInstantHandler` to provide your own implementation of `#inst`.

#### Adding support for your own tags

Use custom handlers may by building an appropriate `ParserConfiguration`:

```java
package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.parser.EdnException;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.ParserConfiguration;
import bpsm.edn.parser.TagHandler;

public class CustomTagHandler {
    @Test
    public void test() throws IOException, URISyntaxException {
        ParserConfiguration cfg =
            ParserConfiguration
                .builder()
                .putTagHandler(Tag.newTag(Symbol.newSymbol("bpsm", "uri")),
                    new TagHandler() {
                        public Object transform(Tag tag, Object value) {
                            try {
                                return new URI((String) value);
                            } catch (URISyntaxException e) {
                                throw new EdnException(e);
                            }
                        }
                    }).build();
        Parser p = Parser.newParser(cfg, "#bpsm/uri \"http://example.com\"");
        assertEquals(new URI("http://example.com"), (URI) p.nextValue());
    }
}

```

#### Using pseudo-tags to influence the parsing of numbers

By default, integers not marked as arbitrary precision by the suffix "N" will parse as `java.lang.Long`. This can be influenced by installing handlers for the tag named by the constant `ParserConfig.LONG_TAG`.

```java
package bpsm.edn.examples;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.ParserConfiguration;
import bpsm.edn.parser.TagHandler;

public class CustomTagHandler {
    @Test
    public void test() throws IOException, URISyntaxException {
        ParserConfiguration cfg =
            ParserConfiguration
                .builder()
                .putTagHandler(Tag.newTag(Symbol.newSymbol("bpsm", "uri")),
                    new TagHandler() {
                        public Object transform(Tag tag, Object value) {
                            return URI.create((String) value);
                        }
                    }).build();
        Parser p = Parser.newParser(cfg, "#bpsm/uri \"http://example.com\"");
        assertEquals(new URI("http://example.com"), (URI) p.nextValue());
    }
}
```

## Printing

The package `bpsm.edn.printer` provides an extensible printer for converting java data structures to valid *edn* text. The default configuration can print values of the following types, as well as Java's `null`, which prints as `nil`:

 - `bpsm.edn.model.Keyword`
 - `bpsm.edn.model.Symbol`
 - `bpsm.edn.model.TaggedValue`
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
import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
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
                protected void eval(URI self, Printer writer)
                    throws IOException {
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

