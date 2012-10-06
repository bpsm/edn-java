// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.parser.Parser.Config.BIG_DECIMAL_TAG;
import static bpsm.edn.parser.Parser.Config.BIG_INTEGER_TAG;
import static bpsm.edn.parser.Parser.Config.DOUBLE_TAG;
import static bpsm.edn.parser.Parser.Config.EDN_INSTANT;
import static bpsm.edn.parser.Parser.Config.EDN_UUID;
import static bpsm.edn.parser.Parser.Config.LONG_TAG;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

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

    public static Parser newParser(Parser.Config cfg) {
        return new ParserImpl(cfg, new Scanner(cfg));
    }

    /**
     * Return a PushbackReader (size 1) for the given Readable.
     * If r is in fact already a PushbackReader, just return r
     * directly.
     * @param r
     * @return
     */
    public static PushbackReader newPushbackReader(final Readable r) {
        if (r instanceof PushbackReader) {
            return (PushbackReader) r;
        }
        if (r instanceof BufferedReader) {
            return new PushbackReader((Reader)r);
        }
        if (r instanceof Reader) {
            return new PushbackReader(new BufferedReader((Reader)r));
        }
        Reader rdr = new Reader() {
            @Override
            public void close() throws IOException {
                if (r instanceof Closeable) {
                    ((Closeable)r).close();
                }
            }

            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return r.read(CharBuffer.wrap(cbuf, off, len));
            }
        };
        return new PushbackReader(new BufferedReader(rdr));
    }

    public static PushbackReader newPushbackReader(final CharSequence cs) {
        if (cs instanceof String) {
            return new PushbackReader(new StringReader((String)cs));
        }
        return newPushbackReader((Readable)CharBuffer.wrap(cs));
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
