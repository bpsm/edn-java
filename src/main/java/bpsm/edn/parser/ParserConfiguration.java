// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import java.util.HashMap;
import java.util.Map;

import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.parser.handlers.InstantToDate;
import bpsm.edn.parser.handlers.UuidHandler;

public class ParserConfiguration {

    BuilderFactory listFactory;
    BuilderFactory vectorFactory;
    BuilderFactory setFactory;
    BuilderFactory mapFactory;
    Map<Tag,TagHandler> tagHandlers;
    boolean shouldInternKeywords = true;
    boolean shouldInternSymbols = false;
    int maxInternedStringLength = 0;

    private ParserConfiguration() {
        listFactory = DEFAULT_LIST_FACTORY;
        vectorFactory = DEFAULT_VECTOR_FACTORY;
        setFactory = DEFAULT_SET_FACTORY;
        mapFactory = DEFAULT_MAP_FACTORY;

        Map<Tag,TagHandler> m = new HashMap<Tag,TagHandler>();
        m.put(EDN_UUID, new UuidHandler());
        m.put(EDN_INSTANT, new InstantToDate());
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
    
    public boolean shouldInternKeywords() {
        return shouldInternKeywords;
    }
    
    public boolean shouldInternSymbols() {
        return shouldInternSymbols;
    }
    
    public int maxInternedStringLength() {
        return maxInternedStringLength;
    }

    public static final Tag EDN_UUID = new Tag(new Symbol(null, "uuid"));
    public static final Tag EDN_INSTANT = new Tag(new Symbol(null, "inst"));

    static final BuilderFactory DEFAULT_LIST_FACTORY = new DefaultListFactory();

    static final BuilderFactory DEFAULT_VECTOR_FACTORY = new DefaultVectorFactory();

    static final BuilderFactory DEFAULT_SET_FACTORY = new DefaultSetFactory();

    static final BuilderFactory DEFAULT_MAP_FACTORY = new DefaultMapFactory();

}
