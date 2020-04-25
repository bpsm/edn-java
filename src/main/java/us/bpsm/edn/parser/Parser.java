// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import static us.bpsm.edn.Tag.newTag;
import us.bpsm.edn.EdnIOException;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;

/**
 * A Parser knows how to parse edn values from any {@link Parseable}.
 * Instances are constructed using factory methods in {@link Parsers}.
 *
 * <p>Any Parser can be shared freely between threads because Parsers are
 * all immutable and thread-safe.
 *
 * @see Parsers
 */
public interface Parser {

    /**
     * The value returned by {@link #nextValue(Parseable)} to indicate
     * that no more values will can be parsed form the a given
     * {@link Parseable}.
     */
    public static final Object END_OF_INPUT = Token.END_OF_INPUT;

    /**
     * Return the next value parsed from {@code pbr}. Calling {@code
     * nextValue} on a Parser created with the default configuration
     * can return any of the following:
     *
     * <ul>
     * <li>A {@link String} with the contents of a string literal.</li>
     *
     * <li>A {@link Character} for a character literal.</li>
     *
     * <li>A {@link Long} for an integer small enough to fit in its
     *     range and not marked by a trailing 'N'.</li>
     *
     * <li>A {@link java.math.BigInteger} for an integer
     *     too large to fit in a Long or marked by a trailing
     *     'N'.</li>
     *
     * <li>A {@link Double} for a binary floating point literal</li>
     *
     * <li>A {@link java.math.BigDecimal} for an arbitrary
     *     precision decimal floating point literal, which is
     *     indicated by a trailing 'M' in edn.</li>
     *
     * <li>A {@link us.bpsm.edn.Symbol} for an edn symbol. ('nil',
     *     'true' and 'false' are not symbols.)</li>
     *
     * <li>A {@link Boolean} for a 'true' or 'false'.
     *
     * <li>A {@code null} for a 'nil'.
     *
     * <li>A {@link us.bpsm.edn.Keyword} for an edn keyword, which
     *     looks like {@code :somename}.</li>
     *
     * <li>The value {@link #END_OF_INPUT} to indicate that no
     *     more values can be parsed form {@code pbr}.</li>
     *
     * <li>A {@link java.util.List} for an <a
     *     href="https://github.com/edn-format/edn#lists">edn
     *     list</a>.
     *
     * <li>A {@link java.util.List} implementing {@link
     *     java.util.RandomAccess RandomAccess} for an <a
     *     href="https://github.com/edn-format/edn#vectors">edn
     *     vector</a>.
     *
     * <li>A {@link java.util.Set} for an <a
     *     href="https://github.com/edn-format/edn#sets">edn set</a>.
     *
     * <li>A {@link java.util.Map} for an <a
     *     href="https://github.com/edn-format/edn#maps">edn map</a>.
     *
     * <li>A {@link java.util.Date} for an <a
     *     href="https://github.com/edn-format/edn#built-in-tagged-elements">
     *     instant literal</a>.</li>
     *
     * <li>A {@link java.util.UUID} for an <a
     *     href="https://github.com/edn-format/edn#built-in-tagged-elements">
     *     uuid literal</a>.</li>
     *
     * </ul>
     * @param pbr parse the next value from this Parseable. Must not be null.
     *
     *
     * @throws EdnIOException
     *             if the underlying Parseable throws an IOException.
     * @throws EdnSyntaxException
     *             if the contents of the underlying Parseable violates the
     *             syntax of edn.
     * @return some Object or {@code null}.
     */
    public Object nextValue(Parseable pbr);

    /**
     * Config describes the complete configuration of a
     * Parser. Instances of Config are constructed by a {@link Builder},
     * provided by {@link Parsers#newParserConfigBuilder()}.
     * <p>
     * A Config provides two kinds of configuration:
     *
     * <ol>
     *
     * <li>One {@link CollectionBuilder.Factory} for building each of:
     *     lists, vectors, sets and maps.</li>
     *
     * <li>A mapping of {@link Tag}s to {@link TagHandler}s:
     *   <ul>
     *
     *     <li> The tags {@code #uuid} and {@code #inst}, which are
     *          defined by edn, are provided with handlers by
     *          default.</li>
     *
     *     <li>The special tags {@link #BIG_DECIMAL_TAG},
     *         {@link #DOUBLE_TAG}, {@link #BIG_INTEGER_TAG} and
     *         {@link #LONG_TAG} can be provided with TagHandlers to
     *         influence how numeric literals are represented by
     *         {@link #nextValue(Parseable)}.</li>
     *
     *    </ul>
     * </li>
     *
     * </ol>
     */
    public interface Config {

        /**
         * This is the {@code #uuid} tag specified by edn.
         */
        public static final Tag EDN_UUID = newTag("uuid");

        /**
         * This is the {@code #inst} tag specified by edn.
         */
        public static final Tag EDN_INSTANT = newTag("inst");

        /**
         * Floating point literals which are marked by a trailing "M"
         * are initially parsed as {@link java.math.BigDecimal}.
         *
         * <p>If you wish to customize the representation of decimal
         * floating point literals, install a TagHandler for this
         * Tag. The result of calling this handler with the parsed
         * {@link java.math.BigDecimal} will be used in the value
         * returned by the parser.
         */
        public static final Tag BIG_DECIMAL_TAG = newTag(
                "us.bpsm.edn-java", "BigDecimal");

        /**
         * Floating point literals not marked by a trailing "M" are
         * initially parsed as {@link Double}.
         *
         * <p>Install a TagHandler for this tag if you wish to
         * customize the representation of floating point
         * literals. The result of calling this handler with the
         * parsed {@link Double} will be used in the value returned by
         * the parser.
         */
        public static final Tag DOUBLE_TAG = newTag(
                "us.bpsm.edn-java", "Double");

        /**
         * Integer literals marked by a trailing "N", and those not so
         * marked which lie outside the range of a {@link Long} are
         * initially parsed as {@link java.math.BigInteger}.
         *
         * <p>Install a TagHandler for this tag if you wish to
         * customize the representation of big integer literals. The
         * result of calling this handler with the parsed
         * {@code BigInteger} will be used in the value returned by
         * the parser.
         */
        public static final Tag BIG_INTEGER_TAG = newTag(
                "us.bpsm.edn-java", "BigInteger");

        /**
         * Integer literals which lie inside the range of a {@link
         * Long} and are not marked by a trailing "N" are initially
         * parsed as {@code Long}.
         *
         * <p>Install a TagHandler for this tag if you wish to
         * customize the representation of long integer literals. The
         * result of calling this handler with the parsed {@link
         * Long} will be used in the value returned by the
         * parser.
         */
        public static final Tag LONG_TAG = newTag(
                "us.bpsm.edn-java", "Long");

        /**
         * Provide a {@link CollectionBuilder.Factory} to receive the
         * contents of a list literal.
         *
         * <p>The default implementation returns an unmodifiable view
         * of a {@link java.util.List} that does not implement {@link
         * java.util.RandomAccess}.
         *
         * @return a CollectionBuilder.Factory for building lists; never null.
         */
        public CollectionBuilder.Factory getListFactory();

        /**
         * Provide a {@link CollectionBuilder.Factory} to receive the
         * contents of a vector literal.
         *
         * <p>The default implementation returns an unmodifiable view
         * of a {@link java.util.List} that implements {@link
         * java.util.RandomAccess}.</p>
         *
         * @return a CollectionBuilder.Factory for building vectors; never null.
         */
        public CollectionBuilder.Factory getVectorFactory();

        /**
         * Provide a {@link CollectionBuilder.Factory} to receive the
         * contents of a set literal.
         *
         * <p>The default implementation returns an unmodifiable view
         * of a {@link java.util.Set} (hashed, not sorted).
         *
         * @return a CollectionBuilder.Factory for building sets; never null.
         */
        public CollectionBuilder.Factory getSetFactory();

        /**
         * Provide a {@link CollectionBuilder.Factory} to receive the
         * contents of a map literal.
         *
         * <p>The default implementation returns an unmodifiable view
         * of a {@link java.util.Map} (hashed, not sorted).
         *
         * @return a CollectionBuilder.Factory for building maps; never null.
         */
        public CollectionBuilder.Factory getMapFactory();

        /**
         * Return the {@link TagHandler} associated with the given
         * {@link Tag}, or null.
         *
         * @param tag must not be null.
         *
         * @return TagHandler associated with {@code tag}, or null.
         */
        public TagHandler getTagHandler(Tag tag);

        /**
         * When true, the parser will accept ∖uXXXX escape sequences in string
         * literals and replace them with the corresponding java char in the
         * parsed string. When false, such escape sequences will throw an.
         * <p>
         * The default is true, which is not in strict accodance with the
         * letter of edn-format/README, but:
         * <ul>
         * <li>Clojure's own edn reader behaves in this way.</li>
         * <li>Character literals do allow this syntax according to
         *     edn-format/README</li>
         * </ul>
         * {@link EdnSyntaxException}.
         * @return
         */
        public default boolean unicodeEscapesInStringLiteralsAreAccepted() {
            return true;
        }

        /**
         * This Builder is used to create a {@link Parser.Config}.
         * Fresh Builder instances are provided by
         * {@link Parsers#newParserConfigBuilder()}.  Each Builder can
         * be used to construct a single {@code Config}.
         *
         * <p>Calling {@code build()} immediately on a new Builder
         * will return an instance of the default configuration.
         */
        public interface Builder {

            /**
             * Use {@code f} to provide {@link CollectionBuilder}s for
             * representing list literals.
             *
             * @param f not null
             *
             * @return this Builder (for method chaining).
             *
             * @throws IllegalStateException if {@code build()} was
             *         previously called on this Builder.
             */
            public Builder setListFactory(CollectionBuilder.Factory f);

            /**
             * Use {@code f} to provide {@link CollectionBuilder}s for
             * representing vector literals.
             *
             * @param f not null
             *
             * @return this Builder (for method chaining).
             *
             * @throws IllegalStateException if {@code build()} was
             *         previously called on this Builder.
             */
            public Builder setVectorFactory(CollectionBuilder.Factory f);

            /**
             * Use {@code f} to provide {@link CollectionBuilder}s for
             * representing set literals.
             *
             * @param f not null
             *
             * @return this Builder (for method chaining).
             *
             * @throws IllegalStateException if {@code build()} was
             *         previously called on this Builder.
             */
            public Builder setSetFactory(CollectionBuilder.Factory f);

            /**
             * Use {@code f} to provide {@link CollectionBuilder}s for
             * representing map literals.
             *
             * @param f not null
             *
             * @return this Builder (for method chaining).
             *
             * @throws IllegalStateException if {@code build()} was
             *         previously called on this Builder.
             */
            public Builder setMapFactory(CollectionBuilder.Factory f);

            /**
             * Register {@code handler} as the handler to be called
             * with {@code tag} is encountered by the parser.
             *
             * @param tag not null
             *
             * @param handler not null
             *
             * @return this Builder (for method chaining).
             *
             * @throws IllegalStateException if {@code build()} was
             *         previously called on this Builder.
             */
            public Builder putTagHandler(Tag tag, TagHandler handler);

            /**
             * Toggle the Parser's willingness to accept unicode escapes
             * in string literals. By default unicode escapes will be
             * accepted.
             * {@link Config#unicodeEscapesInStringLiteralsAreAccepted()}
             */
            public Builder acceptUnicodeEscapesInStringLiterals(
              boolean acceptUnicodeEscapes
            );

            /**
             * Build and return the {@link Config} described by the
             * sequence of calls made on this Builder. Calling
             * {@code build()} invalidates the builder.
             *
             * @return a Config, not null.
             *
             * @throws IllegalStateException if {@code build()} was
             *         previously called on this Builder.
             */
            public Config build();
        }
    }
}
