// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.parser.Parser.Config.BIG_DECIMAL_TAG;
import static bpsm.edn.parser.Parser.Config.BIG_INTEGER_TAG;
import static bpsm.edn.parser.Parser.Config.DOUBLE_TAG;
import static bpsm.edn.parser.Parser.Config.EDN_INSTANT;
import static bpsm.edn.parser.Parser.Config.EDN_UUID;
import static bpsm.edn.parser.Parser.Config.LONG_TAG;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import bpsm.edn.EdnIOException;
import bpsm.edn.Tag;
import bpsm.edn.parser.CollectionBuilder.Factory;
import bpsm.edn.parser.Parser.Config;
import bpsm.edn.parser.Parser.Config.Builder;
import bpsm.edn.parser.inst.InstantToDate;

public class Parsers {


    static final CollectionBuilder.Factory DEFAULT_LIST_FACTORY = new DefaultListFactory();

    static final CollectionBuilder.Factory DEFAULT_VECTOR_FACTORY = new DefaultVectorFactory();

    static final CollectionBuilder.Factory DEFAULT_SET_FACTORY = new DefaultSetFactory();

    static final CollectionBuilder.Factory DEFAULT_MAP_FACTORY = new DefaultMapFactory();

    static final TagHandler INSTANT_TO_DATE = new InstantToDate();

    static final TagHandler UUID_HANDLER = new UuidHandler();

    static final TagHandler IDENTITY = new TagHandler() {
        public Object transform(Tag tag, Object value) {
            return value;
        }
    };

    public static Parser newParser(Parser.Config cfg, Readable readable) {
        try {
            return newParser(cfg, new Scanner(cfg, readable));
        } catch (IOException e) {
            throw new EdnIOException(e);
        }
    }

    public static Parser newParser(Parser.Config cfg, CharSequence input) {
        try {
            return newParser(cfg, new Scanner(cfg, CharBuffer.wrap(input)));
        } catch (IOException e) {
            throw new EdnIOException(e);
        }
    }

    static Parser newParser(final Parser.Config cfg, final Scanner scanner) throws IOException {
        return new ParserImpl(cfg, scanner);
    }

    public static Builder newParserConfigBuilder() {
        return new Builder() {
            boolean used = false;
            CollectionBuilder.Factory listFactory = DEFAULT_LIST_FACTORY;
            CollectionBuilder.Factory vectorFactory = DEFAULT_VECTOR_FACTORY;
            CollectionBuilder.Factory setFactory = DEFAULT_SET_FACTORY;
            CollectionBuilder.Factory mapFactory = DEFAULT_MAP_FACTORY;
            Map<Tag, TagHandler> tagHandlers = defaultTagHandlers();

            public Builder setListFactory(CollectionBuilder.Factory listFactory) {
                checkState();
                this.listFactory = listFactory;
                return this;
            }

            public Builder setVectorFactory(CollectionBuilder.Factory vectorFactory) {
                checkState();
                this.vectorFactory = vectorFactory;
                return this;
            }

            public Builder setSetFactory(CollectionBuilder.Factory setFactory) {
                checkState();
                this.setFactory = setFactory;
                return this;
            }

            public Builder setMapFactory(CollectionBuilder.Factory mapFactory) {
                checkState();
                this.mapFactory = mapFactory;
                return this;
            }

            public Builder putTagHandler(Tag tag, TagHandler handler) {
                checkState();
                this.tagHandlers.put(tag, handler);
                return this;
            }

            public Config build() {
                checkState();
                used = true;
                return new Config() {
                    public Factory getListFactory() {
                        return listFactory;
                    }

                    public Factory getVectorFactory() {
                        return vectorFactory;
                    }

                    public Factory getSetFactory() {
                        return setFactory;
                    }

                    public Factory getMapFactory() {
                        return mapFactory;
                    }

                    public TagHandler getTagHandler(Tag tag) {
                        return tagHandlers.get(tag);
                    }
                };
            }

            private void checkState() {
                if (used) {
                    throw new IllegalStateException("Builder is single-use. Not usable after build()");
                }
            }
        };
    }

    static Map<Tag, TagHandler> defaultTagHandlers() {
        Map<Tag, TagHandler> m = new HashMap<Tag, TagHandler>();
        m.put(EDN_UUID, UUID_HANDLER);
        m.put(EDN_INSTANT, INSTANT_TO_DATE);
        m.put(BIG_DECIMAL_TAG, IDENTITY);
        m.put(DOUBLE_TAG, IDENTITY);
        m.put(BIG_INTEGER_TAG, IDENTITY);
        m.put(LONG_TAG, IDENTITY);
        return m;
    }

    public static Config defaultConfiguration() {
        return DEFAULT_CONFIGURATION;
    }

    static Config DEFAULT_CONFIGURATION = newParserConfigBuilder().build();

}
