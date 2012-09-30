package bpsm.edn.parser;

import java.util.Collections;

import bpsm.edn.model.Tag;

public class ParserConfigurationBuilder {
    ParserConfiguration pc;

    ParserConfigurationBuilder(ParserConfiguration pc) {
        assert pc != null;
        this.pc = pc;
    }

    public ParserConfigurationBuilder setListFactory(CollectionBuilder.Factory listFactory) {
        checkState();
        pc.listFactory = listFactory;
        return this;
    }

    public ParserConfigurationBuilder setVectorFactory(CollectionBuilder.Factory vectorFactory) {
        checkState();
        pc.vectorFactory = vectorFactory;
        return this;
    }

    public ParserConfigurationBuilder setSetFactory(CollectionBuilder.Factory setFactory) {
        checkState();
        pc.setFactory = setFactory;
        return this;
    }

    public ParserConfigurationBuilder setMapFactory(CollectionBuilder.Factory mapFactory) {
        checkState();
        pc.mapFactory = mapFactory;
        return this;
    }

    public ParserConfigurationBuilder putTagHandler(Tag tag, TagHandler handler) {
        checkState();
        pc.tagHandlers.put(tag, handler);
        return this;
    }

    public ParserConfiguration build() {
        checkState();
        pc.tagHandlers = Collections.unmodifiableMap(pc.tagHandlers);
        ParserConfiguration result = pc;
        pc = null;
        return result;
    }

    private void checkState() {
        if (pc == null) {
            throw new IllegalStateException("Builder is single-use. Not usable after build()");
        }
    }
}