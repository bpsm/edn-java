// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.Symbol.newSymbol;
import static bpsm.edn.Tag.newTag;

import java.io.PushbackReader;

import bpsm.edn.Tag;

public interface Parser {

    public static final Object END_OF_INPUT = Token.END_OF_INPUT;

    public Object nextValue(Parseable pbr);

    public interface Config {

        public static final Tag EDN_UUID = newTag(newSymbol(null, "uuid"));

        public static final Tag EDN_INSTANT = newTag(newSymbol(null, "inst"));

        /**
         * Floating point literals which are marked by a trailing M are
         * initially parsed as {@code BigDecimal}. A user wishing to customize
         * the representation used for big decimals may install a TagHandler for
         * this Tag, which will be called with the parsed BigDecimal.
         */
        public static final Tag BIG_DECIMAL_TAG = newTag(newSymbol(
                "info.bsmithmannschott.edn-java", "BigDecimal"));

        /**
         * Floating point literals not marked by a trailing M are initially
         * parsed as {@code Double}. A user wishing to customize the
         * representation used for doubles may install a TagHandler for this
         * Tag, which will be called with the parsed Double.
         */
        public static final Tag DOUBLE_TAG = newTag(newSymbol(
                "info.bsmithmannschott.edn-java", "Double"));

        /**
         * Integer literals marked by a trailing N, and those which lie outside
         * the range of a {@code java.lang.Long} are initially parsed as
         * {@code BigInteger.} A user wishing to customize the representation of
         * big integers may install a TagHandler for this Tag, which will be
         * called with the parsed BigInteger.
         */
        public static final Tag BIG_INTEGER_TAG = newTag(newSymbol(
                "info.bsmithmannschott.edn-java", "BigInteger"));

        /**
         * Integer literals not marked by a trailing N which lie inside the
         * range of a {@code java.lang.Long} are initially parsed as
         * {@code Long}. A user wishing to customize the representation of big
         * integers may install a TagHandler for this Tag, which will be called
         * with the parsed BigInteger.
         */
        public static final Tag LONG_TAG = newTag(newSymbol(
                "info.bsmithmannschott.edn-java", "Long"));

        public CollectionBuilder.Factory getListFactory();

        public CollectionBuilder.Factory getVectorFactory();

        public CollectionBuilder.Factory getSetFactory();

        public CollectionBuilder.Factory getMapFactory();

        public TagHandler getTagHandler(Tag tag);

        public interface Builder {

            public Builder setListFactory(CollectionBuilder.Factory f);

            public Builder setVectorFactory(CollectionBuilder.Factory f);

            public Builder setSetFactory(CollectionBuilder.Factory f);

            public Builder setMapFactory(CollectionBuilder.Factory f);

            public Builder putTagHandler(Tag tag, TagHandler handler);

            public Config build();

        }
    }
}
