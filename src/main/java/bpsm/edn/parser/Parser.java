// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

import static bpsm.edn.parser.Token.END_LIST;
import static bpsm.edn.parser.Token.END_MAP_OR_SET;
import static bpsm.edn.parser.Token.END_OF_INPUT;
import static bpsm.edn.parser.Token.END_VECTOR;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import bpsm.edn.model.Tag;
import bpsm.edn.model.TaggedValue;

public class Parser implements Closeable {
    private ParserConfiguration cfg;
    private Scanner scanner;
    private Object curr;
    private int discard;

    Parser(ParserConfiguration cfg, Scanner scanner) throws IOException {
        this.scanner = scanner;
        this.curr = scanner.nextToken();
        this.cfg = cfg;
        this.discard = 0;
    }
    
    public void close() throws IOException {
        if (scanner != null) {
            scanner.close();
            scanner = null;
        }
    }

    public static Parser newParser(ParserConfiguration cfg, Reader reader) throws IOException {
        return new Parser(cfg, new Scanner(cfg, reader));
    }
    
    public static Parser newParser(ParserConfiguration cfg, CharSequence input) throws IOException {
        return new Parser(cfg, new Scanner(cfg, CharSequenceReader.newCharSequenceReader(input)));
    }

    /**
     * Equivalent to:
     * {@code newParser(ParserConfiguration.defaultConfiguration(), in)}
     * @param in
     * @return
     */
    public static Parser newParser(Reader reader) throws IOException {
        return newParser(ParserConfiguration.defaultConfiguration(), reader);
    }

    public ParserConfiguration getConfiguration() {
        return cfg;
    }

    public void setConfiguration(ParserConfiguration cfg) {
        if (cfg == null) {
            throw new NullPointerException();
        }
        this.cfg = cfg;
    }

    public Object nextValue() throws IOException {
        assert discard >= 0;
        if (curr instanceof Token) {
            switch ((Token) curr) {
            case BEGIN_LIST:
                nextToken();
                return parseIntoCollection(cfg.getListFactory(), END_LIST);
            case BEGIN_VECTOR:
                nextToken();
                return parseIntoCollection(cfg.getVectorFactory(), END_VECTOR);
            case BEGIN_SET:
                nextToken();
                return parseIntoCollection(cfg.getSetFactory(), END_MAP_OR_SET);
            case BEGIN_MAP:
                nextToken();
                return parseIntoMap(cfg.getMapFactory());
            case DISCARD:
                nextToken();
                discardValue();
                return nextValue();
            case END_LIST:
            case END_MAP_OR_SET:
            case END_VECTOR:
                throw new EdnException();
            case END_OF_INPUT:
                return END_OF_INPUT;
            case NIL:
                nextToken();
                return null;
            default:
                throw new EdnException();
            }
        } else if (curr instanceof Tag) {
            Tag t = (Tag) curr;
            nextToken();
            return nextValue(t);
        } else {
            Object value = curr;
            nextToken();
            return value;
        }
    }

    private Object nextToken() throws IOException {
        try {
            return curr = scanner.nextToken();
        } catch (IOException e) {
            curr = Token.END_OF_INPUT;
            try {
                scanner.close();
            } catch (IOException _) {
                // suppress _ in favor of e
            }
            throw e;
        }
    }

    private Object nextValue(Tag t) throws IOException {
        Object v = nextValue();
        if (discard == 0) {
            TagHandler x = cfg.getTagHandlers().get(t);
            if (x != null) {
                return x.transform(t, v);
            } else {
                return new TaggedValue(t, v);
            }
        } else {
            return null;
        }
    }

    private void discardValue() throws IOException {
        try {
            discard ++;
            nextValue();
        } finally {
            discard --;
        }
    }

    private Object parseIntoMap(BuilderFactory f) throws IOException {
        CollectionBuilder b = (discard == 0) ? f.builder() : null;
        while (curr != END_MAP_OR_SET) {
            Object o = nextValue();
            if (o == END_OF_INPUT) {
                throw new EdnException("Expected '}', but found end of input.\n" +
                        String.valueOf(b.build()));
            }
            if (discard == 0) {
                b.add(o);
            }
        }
        nextToken();
        return (discard == 0) ? b.build() : null;
    }

    private Object parseIntoCollection(BuilderFactory f, Token end) throws IOException {
        CollectionBuilder b = (discard == 0) ? f.builder() : null;
        while (curr != end) {
            Object value = nextValue();
            if (value == END_OF_INPUT) {
                throw new EdnException();
            }
            if (discard == 0) {
                b.add(value);
            }
        }
        nextToken();
        return (discard == 0) ? b.build() : null;
    }


}
