// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.model.Symbol.newSymbol;
import static bpsm.edn.model.Tag.newTag;

import java.util.HashMap;
import java.util.Map;

import bpsm.edn.model.Tag;
import bpsm.edn.parser.handlers.InstantToDate;
import bpsm.edn.parser.handlers.UuidHandler;

public class ParserConfiguration {
    /**
     * Floating point literals which are marked by a trailing M are initially
     * parsed as {@code BigDecimal}. A user wishing to customize the
     * representation used for big decimals may install a TagHandler for this
     * Tag, which will be called with the parsed BigDecimal.
     */
    public static final Tag BIG_DECIMAL_TAG = synTag("BigDecimal");

    /**
     * Floating point literals not marked by a trailing M are initially parsed
     * as {@code Double}. A user wishing to customize the representation used
     * for doubles may install a TagHandler for this Tag, which will be called
     * with the parsed Double.
     */
    public static final Tag DOUBLE_TAG = synTag("Double");

    /**
     * Integer literals marked by a trailing N, and those which lie outside the
     * range of a {@code java.lang.Long} are initially parsed as
     * {@code BigInteger.} A user wishing to customize the representation of big
     * integers may install a TagHandler for this Tag, which will be called with
     * the parsed BigInteger.
     */
    public static final Tag BIG_INTEGER_TAG = synTag("BigInteger");

    /**
     * Integer literals not marked by a trailing N which lie inside the range of
     * a {@code java.lang.Long} are initially parsed as {@code Long}. A user
     * wishing to customize the representation of big integers may install a
     * TagHandler for this Tag, which will be called with the parsed BigInteger.
     */
    public static final Tag LONG_TAG = synTag("Long");

    public static final Tag EDN_UUID = newTag(newSymbol(null, "uuid"));

    public static final Tag EDN_INSTANT = newTag(newSymbol(null, "inst"));

    static final BuilderFactory DEFAULT_LIST_FACTORY = new DefaultListFactory();

    static final BuilderFactory DEFAULT_VECTOR_FACTORY =
            new DefaultVectorFactory();

    static final BuilderFactory DEFAULT_SET_FACTORY = new DefaultSetFactory();

    static final BuilderFactory DEFAULT_MAP_FACTORY = new DefaultMapFactory();

    static final TagHandler INSTANT_TO_DATE = new InstantToDate();

    static final TagHandler UUID_HANDLER = new UuidHandler();

    static final TagHandler IDENTITY = new TagHandler() {
        public Object transform(Tag tag, Object value) {
            return value;
        }
    };
    
    private static Tag synTag(String name) {
        return newTag(newSymbol("info.bsmithmannschott.edn-java", name));
    }
    
    
    BuilderFactory listFactory;
    BuilderFactory vectorFactory;
    BuilderFactory setFactory;
    BuilderFactory mapFactory;
    Map<Tag, TagHandler> tagHandlers;

    private ParserConfiguration() {
        listFactory = DEFAULT_LIST_FACTORY;
        vectorFactory = DEFAULT_VECTOR_FACTORY;
        setFactory = DEFAULT_SET_FACTORY;
        mapFactory = DEFAULT_MAP_FACTORY;

        Map<Tag, TagHandler> m = new HashMap<Tag, TagHandler>();
        m.put(EDN_UUID, UUID_HANDLER);
        m.put(EDN_INSTANT, INSTANT_TO_DATE);
        m.put(BIG_DECIMAL_TAG, IDENTITY);
        m.put(DOUBLE_TAG, IDENTITY);
        m.put(BIG_INTEGER_TAG, IDENTITY);
        m.put(LONG_TAG, IDENTITY);
        this.tagHandlers = m;
    }

    public static ParserConfiguration defaultConfiguration() {
        return builder().build();
    }

    public static ParserConfigurationBuilder builder() {
        return new ParserConfigurationBuilder(new ParserConfiguration());
    }

    public BuilderFactory getListFactory() {
        return listFactory;
    }

    public BuilderFactory getVectorFactory() {
        return vectorFactory;
    }

    public BuilderFactory getSetFactory() {
        return setFactory;
    }

    public BuilderFactory getMapFactory() {
        return mapFactory;
    }

    public Map<Tag, TagHandler> getTagHandlers() {
        return tagHandlers;
    }

}
