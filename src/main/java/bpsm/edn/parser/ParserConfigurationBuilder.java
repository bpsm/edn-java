package bpsm.edn.parser;

import java.util.Collections;

import bpsm.edn.model.Tag;

public class ParserConfigurationBuilder {
    ParserConfiguration pc;

    ParserConfigurationBuilder(ParserConfiguration pc) {
        assert pc != null;
        this.pc = pc;
    }

    public ParserConfigurationBuilder setListFactory(BuilderFactory listFactory) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.listFactory = listFactory;
        return this;
    }

    public ParserConfigurationBuilder setVectorFactory(BuilderFactory vectorFactory) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.vectorFactory = vectorFactory;
        return this;
    }

    public ParserConfigurationBuilder setSetFactory(BuilderFactory setFactory) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.setFactory = setFactory;
        return this;
    }

    public ParserConfigurationBuilder setMapFactory(BuilderFactory mapFactory) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.mapFactory = mapFactory;
        return this;
    }

    public ParserConfigurationBuilder putTagHandler(Tag tag, TagHandler handler) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.tagHandlers.put(tag, handler);
        return this;
    }
    
    public ParserConfigurationBuilder shouldInternKeywords(boolean f) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.shouldInternKeywords = f;
        return this;
    }
    
    public ParserConfigurationBuilder shouldInternSymbols(boolean f) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.shouldInternSymbols = f;
        return this;
    }
    
    public ParserConfigurationBuilder setMaxInternedStringLength(int maxLen) {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.maxInternedStringLength = maxLen;
        return this;
    }

    public ParserConfiguration build() {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
        pc.tagHandlers = Collections.unmodifiableMap(pc.tagHandlers);
        ParserConfiguration result = pc;
        pc = null;
        return result;
    }
}