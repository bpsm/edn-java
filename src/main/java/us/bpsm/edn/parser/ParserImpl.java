// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import static us.bpsm.edn.TaggedValue.newTaggedValue;
import static us.bpsm.edn.parser.Token.END_LIST;
import static us.bpsm.edn.parser.Token.END_MAP_OR_SET;
import static us.bpsm.edn.parser.Token.END_VECTOR;

import us.bpsm.edn.*;


class ParserImpl implements Parser {

    private static final Object DISCARDED_VALUE = new Object() {
        @Override
        public String toString() { return "##discarded value##"; }
    };

    private Config cfg;
    private Scanner scanner;

    ParserImpl(Config cfg, Scanner scanner) {
        this.scanner = scanner;
        this.cfg = cfg;
    }

    public Object nextValue(Parseable pbr) {
        Object value = nextValue(pbr, false);
        if (value instanceof Token && value != END_OF_INPUT) {
            throw new EdnSyntaxException("Unexpected "+ value);
        }
        return value;
    }

    private Object nextValue(Parseable pbr, boolean discard) {
        Object curr = scanner.nextToken(pbr);
        if (curr instanceof Token) {
            switch ((Token) curr) {
            case BEGIN_LIST:
                return parseIntoCollection(cfg.getListFactory(),
                                           END_LIST, pbr, discard);
            case BEGIN_VECTOR:
                return parseIntoCollection(cfg.getVectorFactory(),
                                           END_VECTOR, pbr, discard);
            case BEGIN_SET:
                return parseIntoCollection(cfg.getSetFactory(),
                                           END_MAP_OR_SET, pbr, discard);
            case BEGIN_MAP:
                return parseIntoCollection(cfg.getMapFactory(),
                                           END_MAP_OR_SET, pbr, discard);
            case DEFAULT_NAMESPACE_FOLLOWS: {
                final String ns = parseNamespaceName(pbr, discard);
                Object t = scanner.nextToken(pbr);
                if (t != Token.BEGIN_MAP) {
                    throw new EdnSyntaxException(
                      "Expected #:" + ns + " to be followed by a map.");
                }
                return parseIntoCollection(new NamespacedMapFactory(ns),
                  END_MAP_OR_SET, pbr, discard);
            }
            case DISCARD:
                nextValue(pbr, true);
                return nextValue(pbr, discard);
            case NIL:
                return null;
            case END_OF_INPUT:
            case END_LIST:
            case END_MAP_OR_SET:
            case END_VECTOR:
                return curr;
            default:
                throw new EdnSyntaxException("Unrecognized Token: " + curr);
            }
        } else if (curr instanceof Tag) {
            return nextValue((Tag)curr, pbr, discard);
        } else {
            return curr;
        }
    }

    private String parseNamespaceName(Parseable pbr, boolean discard) {
        final Object nsObj = nextValue(pbr, discard);
        if (!(nsObj instanceof Symbol)) {
            throw new EdnSyntaxException(
              "Expected symbol following #:, but found: " + nsObj);
        }
        final Symbol nsSym = (Symbol) nsObj;
        if (nsSym.getPrefix().length() > 0) {
            throw new EdnSyntaxException(
              "Expected symbol following #: to be namespaceless, " +
                "but found: " + nsSym);
        }
        return nsSym.getName();
    }

    private Object nextValue(Tag t, Parseable pbr, boolean discard) {
        Object v = nextValue(pbr, discard);
        if (discard) {
            // It doesn't matter what we return here, as it will be discarded.
            return DISCARDED_VALUE;
        }
        TagHandler x = cfg.getTagHandler(t);
        return x != null ? x.transform(t, v) : newTaggedValue(t, v);
    }

    private Object parseIntoCollection(CollectionBuilder.Factory f, Token end,
                                       Parseable pbr, boolean discard) {
        CollectionBuilder b = !discard ? f.builder() : null;
        for (Object o = nextValue(pbr, discard); 
             o != end; 
             o = nextValue(pbr, discard)) {
            if (o instanceof Token) {
                throw new EdnSyntaxException("Expected " + end +
                                             ", but found " + o);
            }
            if (!discard) {
                b.add(o);
            }
        }
        return !discard ? b.build() : null;
    }

    private class NamespacedMapFactory implements CollectionBuilder.Factory {
        private final String defaultNs;

        public NamespacedMapFactory(String defaultNs) {
            this.defaultNs = defaultNs;
        }

        @Override
        public CollectionBuilder builder() {
            return new NamespacedMapBuilder();
        }

        private class NamespacedMapBuilder implements CollectionBuilder {
            private final CollectionBuilder cfgBuilder;
            boolean key;

            public NamespacedMapBuilder() {
                this.cfgBuilder = cfg.getMapFactory().builder();;
                key = true;
            }

            @Override
            public void add(Object o) {
                if (key) {
                    if (o instanceof Symbol || o instanceof Keyword) {
                        final Named n = (Named) o;
                        final String p = n.getPrefix(), ns;
                        if ("".equals(p)) {
                            ns = defaultNs;
                        } else if ("_".equals(p)) {
                            ns = "";
                        } else {
                            ns = null;
                        }
                        if (ns != null) {
                            if (o instanceof Symbol) {
                                o = Symbol.newSymbol(ns, n.getName());
                            } else {
                                assert o instanceof Keyword;
                                o = Keyword.newKeyword(ns, n.getName());
                            }
                        }
                    }
                }
                key = !key;
                cfgBuilder.add(o);
            }

            @Override
            public Object build() {
                return cfgBuilder.build();
            }
        }
    }
}
