# edn-java

*edn-java* is a library to read [edn](https://github.com/edn-format/edn).

This is very early days. Consider all of this library subject to change, incomplete and probably buggy. There are still rough edges in the design which may see me moving things around some before I'm happy with the result.

## Installation

This is a Maven project with the following coordinates:

    <dependency>
        <groupId>info.bsmithmannschott</groupId>
        <artifactId>edn-java</artifactId>
        <version>0.1-SNAPSHOT</version>
    </dependency>

You'll have to build it form source yourself using Maven.  This library is not currently available in any public repository.

## Usage

You'll need to create a Parser and supply it with some Input.

    import bpsm.edn.parser.Parser;
    import bpsm.edn.parser.input.Input;
    import bpsm.edn.parser.input.CharSequenceInput;
    import bpsm.edn.parser.scanner.Token;

    ...
    Input input = new CharSequenceInput("{:x 1 :y 2}");
    Parser parser = Parser.newParser(input);

You can then call `nextValue()` to read values form the input.

    parser.nextValue(); // returns a map with :x->1 and y->2
    parser.nextValue(); // returns Token.END_OF_INPUT;

If the input is exhausted, `END_OF_INPUT` is returned. An Input implementation which wraps a `java.io.Reader` is also provided.

## Mapping from EDN to Java

Most *edn* values map to regular Java types, except in such cases where Java doesn't provide something suitable. Implementations are provided in the package `bpsm.edn.model`.

`Symbol` and `Keyword` have an optional `prefix` and a mandatory `name`. Both implement the interface `Named`.

Integers map to `Integer`, `Long` or `BigInteger` depending on the magnitude of the number. Appending `N` to an integer literal maps to `BigInteger` irrespective of the magnitude.

Floating point numbers with the suffix `M` are  mapeped to `BigDecimal`. All others are mapped to `Double`.

Characters are mapped to `Character`, booleans to `Boolean` and strings to `String`. No great shock there, I trust.

Lists and vectors are both mapped to implementations of `java.util.List`. List maps to `java.util.LinkedList`, while vector maps to `java.util.ArrayList`.

Maps map to `java.util.HashMap` and sets to `java.util.HashSet`.

The parser is customized by providing it with a ParserConfiguration when you create it:

    Parser.newParser(ParserConfiguration.defaultConfiguration(), input)

The parser can be customized to use different collection classes by first building the appropriate `ParserConfiguration`:

    ParserConfiguration cfg =
        ParserConfiguration.builder()
            .setListFactory( new BuilderFactory() { ... } )
            ...
            .build();

    Parser.newParser(cfg, input).nextValue();

## Tagged Values

Tagged values with an unrecognized tag are mapped to `bpsm.edn.model.TaggedValue`.

By default, handlers are provided automatically for `#inst` and `#uuid`, which return a `java.util.Date` and a `java.util.UUID` respectively.

Three handlers for `#inst` are available:

 - `InstantToDate` is the default and converts each `#inst` to a `java.util.Date`.
 - `InstantToCalendar` converts each `#inst` to a `java.util.Calendar`, which preserves the original time zone.
 - `InstantToTimestamp` converts each `#inst` to a `java.sql.Timstamp`, which presrves nanoseconds.

Extend `AbstractInstantHandler` to provide your own implementation of `#inst`.

Use custom handlers may by building an appropriate `ParserConfiguration`:

    ParserConfiguration.builder()
        .putTagHandler(new Tag("bpsm", "url"),
                       new TagHandler() {
                           public Object transform(Tag tag, Object value) {
                               return new URL((String)value);
                           }
                       })
        .putTagHadler(ParserConfiguration.EDN_INSTANT,
                      new InstantToCalendar())
        .build();


