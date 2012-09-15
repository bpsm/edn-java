// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.parser.handlers.InstantToDate;
import bpsm.edn.parser.handlers.UuidHandler;

public class ParserConfiguration {
    
    private BuilderFactory listFactory = DEFAULT_LIST_FACTORY;
    private BuilderFactory vectorFactory = DEFAULT_VECTOR_FACTORY;
    private BuilderFactory setFactory = DEFAULT_SET_FACTORY;
    private BuilderFactory mapFactory = DEFAULT_MAP_FACTORY;
    private Map<Tag,TagHandler> tagHandlers =
            new HashMap<Tag,TagHandler>(DEFAULT_TAG_HANDLERS);

    public BuilderFactory getListFactory() {
        return listFactory;
    }

    public void setListFactory(BuilderFactory listFactory) {
        this.listFactory = listFactory;
    }

    public BuilderFactory getVectorFactory() {
        return vectorFactory;
    }

    public void setVectorFactory(BuilderFactory vectorFactory) {
        this.vectorFactory = vectorFactory;
    }

    public BuilderFactory getSetFactory() {
        return setFactory;
    }

    public void setSetFactory(BuilderFactory setFactory) {
        this.setFactory = setFactory;
    }

    public BuilderFactory getMapFactory() {
        return mapFactory;
    }

    public void setMapFactory(BuilderFactory mapFactory) {
        this.mapFactory = mapFactory;
    }

    public Map<Tag, TagHandler> getTagHandlers() {
        return tagHandlers;
    }

    public void setTagHandlers(Map<Tag, TagHandler> tagHandlers) {
        this.tagHandlers = tagHandlers;
    }

    public static final Tag EDN_UUID = new Tag(new Symbol(null, "uuid"));
    public static final Tag EDN_INSTANT = new Tag(new Symbol(null, "inst"));
    public static final Map<Tag, TagHandler> DEFAULT_TAG_HANDLERS = createDefaultTagHandlers();
    
    private static Map<Tag, TagHandler> createDefaultTagHandlers() {
        Map<Tag,TagHandler> m = new HashMap<Tag,TagHandler>();
        m.put(EDN_UUID, new UuidHandler());
        m.put(EDN_INSTANT, new InstantToDate());
        return Collections.unmodifiableMap(m);
    }
    
    public static final BuilderFactory DEFAULT_LIST_FACTORY = new BuilderFactory() {
        public CollectionBuilder builder() {
            return new CollectionBuilder() {
                LinkedList<Object> list = new LinkedList<Object>();
                public void add(Object o) {
                    list.add(o);
                }
                public Object build() {
                    return list;
                }
            };
        }
    };
    
    public static final BuilderFactory DEFAULT_VECTOR_FACTORY = new BuilderFactory() {
        public CollectionBuilder builder() {
            return new CollectionBuilder() {
                ArrayList<Object> list = new ArrayList<Object>();
                public void add(Object o) {
                    list.add(o);
                }
                public Object build() {
                    return list;
                }
            };
        }
    };
    
    public static final BuilderFactory DEFAULT_SET_FACTORY = new BuilderFactory() {
        public CollectionBuilder builder() {
            return new CollectionBuilder() {
                Set<Object> set = new HashSet<Object>();
                public void add(Object o) {
                    set.add(o);
                }
                public Object build() {
                    return set;
                }
            };
        }
    };
    
    public static final BuilderFactory DEFAULT_MAP_FACTORY = new BuilderFactory() {
        public CollectionBuilder builder() {
            return new CollectionBuilder() {
                Map<Object,Object> map = new HashMap<Object,Object>();
                Object key = NONE;
                public void add(Object o) {
                    if (key == NONE) {
                        key = o;
                    } else {
                        map.put(key, o);
                        key = NONE;
                    }
                }
                public Object build() {
                    if (key != NONE) {
                        throw new IllegalStateException(
                                "Every map must have an equal number of keys and values.");
                    }
                    return map;
                }
            };
        }
    };
    
    static final Object NONE = new Object();
    
}
