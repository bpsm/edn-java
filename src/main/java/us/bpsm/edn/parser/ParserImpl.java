// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import static us.bpsm.edn.TaggedValue.newTaggedValue;
import static us.bpsm.edn.parser.Token.END_LIST;
import static us.bpsm.edn.parser.Token.END_MAP_OR_SET;
import static us.bpsm.edn.parser.Token.END_VECTOR;
import us.bpsm.edn.EdnSyntaxException;
import us.bpsm.edn.Tag;


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

    @Override
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

}
