package bpsm.edn.printer;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import java.util.UUID;

import bpsm.edn.model.EdnException;
import bpsm.edn.model.Keyword;
import bpsm.edn.model.Symbol;
import bpsm.edn.model.Tag;
import bpsm.edn.model.TaggedValue;
import bpsm.edn.parser.Parser;
import bpsm.edn.parser.handlers.InstantUtils;
import bpsm.edn.parser.util.CharClassify;
import bpsm.edn.protocols.Function;
import bpsm.edn.protocols.Protocol;
import bpsm.edn.protocols.Protocols;

public class Printers {

    public static Printer newPrinter(final Printer.Config cfg,
            final Writer writer) {
        return new Printer() {
            int softspace = 0;
            
            public void close() throws IOException {
                writer.close();
            }

            public Printer append(CharSequence csq) throws IOException {
                if (softspace > 1 && csq.length() > 0 && !CharClassify.isWhitespace(csq.charAt(0))) {
                    writer.append(' ');
                }
                softspace = 0;
                writer.append(csq);
                return this;
            }

            public Printer append(char c) throws IOException {
                if (softspace > 1 && !CharClassify.isWhitespace(c)) {
                    writer.append(' ');
                }
                softspace = 0;
                writer.append(c);
                return this;
            }

            public Printer append(CharSequence csq, int start, int end)
                    throws IOException {
                if (softspace > 1 && end - start > 0 && !CharClassify.isWhitespace(csq.charAt(start))) {
                    writer.append(' ');
                }
                softspace = 0;
                writer.append(csq, start, end);
                return this;
            }

            public Printer printValue(Object ednValue) throws IOException {
                Function printFn = cfg.getPrintFn(ednValue);
                if (printFn == null) {
                    throw new EdnException(String.format(
                            "Don't know how to write '%s' of type '%s'",
                            ednValue, Util.getClassOrNull(ednValue)));
                }
                try {
                    printFn.eval(ednValue, this);
                } catch (PrintException e) {
                    throw (IOException) e.getCause();
                }
                return this;
            }

            public Printer softspace() {
                softspace += 1;
                return this;
            }

        };
    }

    public static Printer.Config.Builder newPrinterConfigBuilder() {
        return new Printer.Config.Builder() {
            Protocol.Builder protocolBuilder = Protocols.builder("print")
                    .put(null, writeNullFn())
                    .put(List.class, writeListFn())
                    .put(Map.class, writeMapFn())
                    .put(Set.class, writeSetFn())
                    .put(Keyword.class, writeKeywordFn())
                    .put(Symbol.class, writeSymbolFn())
                    .put(CharSequence.class, writeCharSequenceFn())
                    .put(Character.class, writeCharacterFn())
                    .put(Boolean.class, writeBooleanFn())
                    .put(Byte.class, writeLongValueFn())
                    .put(Short.class, writeLongValueFn())
                    .put(Integer.class, writeLongValueFn())
                    .put(Long.class, writeLongValueFn())
                    .put(BigInteger.class, writeBigIntegerFn())
                    .put(Float.class, writeDoubleValueFn())
                    .put(Double.class, writeDoubleValueFn())
                    .put(BigDecimal.class, writeBigDecimalFn())
                    .put(UUID.class, writeUuidFn())
                    .put(Date.class, writeDateFn())
                    .put(Timestamp.class, writeTimestampFn())
                    .put(GregorianCalendar.class, writeCalendarFn())
                    .put(TaggedValue.class, writeTaggedValueFn())
                    .put(Tag.class, writeTagFn());

            @SuppressWarnings("rawtypes")
            public Printer.Config.Builder bind(Class ednValueClass,
                    Function printFn) {
                protocolBuilder.put(ednValueClass, printFn);
                return this;
            }

            public Printer.Config build() {
                return new Printer.Config() {
                    Protocol protocol = protocolBuilder.build();

                    public Function getPrintFn(Object ednValue) {
                        return protocol.lookup(Util.getClassOrNull(ednValue));
                    }
                };
            }

        };
    }
    
    public static Printer.Config defaultPrinterConfig() {
        return newPrinterConfigBuilder().build();
    }
    
    static Function writeNullFn() {
        return new PrintFn<Void>() {
            @Override
            protected void eval(Void self, Printer writer) throws IOException {
                writer.softspace().append("nil").softspace();
            }
        };
    }
    
    static Function writeListFn() {
        return new PrintFn<List<?>>() {
            @Override
            protected void eval(List<?> self, Printer writer)
                    throws IOException {
                boolean vec = self instanceof RandomAccess;
                writer.append(vec ? "[" : "(");
                for (Object o: self) {
                    writer.printValue(o);
                }
                writer.append(vec ? "]" : ")");
            }
        };
    }
    
    static Function writeSetFn() {
        return new PrintFn<Set<?>>() {
            @Override
            protected void eval(Set<?> self, Printer writer)
                    throws IOException {
                writer.append("#{");
                for (Object o: self) {
                    writer.printValue(o);
                }
                writer.append("}");
            }            
        };
    }
    
    static Function writeMapFn() {
        return new PrintFn<Map<?,?>>() {
            @Override
            protected void eval(Map<?,?> self, Printer writer)
                    throws IOException {
                writer.append("{");
                for (Map.Entry<?,?> p: self.entrySet()) {
                    writer.printValue(p.getKey())
                        .printValue(p.getValue());
                }
                writer.append("}");
            }            
        };
    }
    
    static Function writeKeywordFn() {
        return new PrintFn<Keyword>() {
            @Override
            protected void eval(Keyword self, Printer writer)
                    throws IOException {
                writer.softspace().append(self.toString()).softspace(); 
            }
        };
    }
    
    static Function writeSymbolFn() {
        return new PrintFn<Symbol>() {
            @Override
            protected void eval(Symbol self, Printer writer)
                    throws IOException {
                writer.softspace().append(self.toString()).softspace(); 
            }
        };
    }
    
    static Function writeTaggedValueFn() {
        return new PrintFn<TaggedValue>() {
            @Override
            protected void eval(TaggedValue self, Printer writer)
                    throws IOException {
                writer.printValue(self.getTag()).printValue(self.getValue());
            }
        };
    }
    
    static Function writeBooleanFn() {
        return new PrintFn<Boolean>() {
            @Override
            protected void eval(Boolean self, Printer writer)
                    throws IOException {
                writer.softspace()
                    .append(self ? "true" : "false")
                    .softspace();
            }
        };
    }
    
    static Function writeCharSequenceFn() {
        return new PrintFn<CharSequence>() {
            @Override
            protected void eval(CharSequence self, Printer writer)
                    throws IOException {
                writer.append('"');
                for (int i = 0; i < self.length(); i++) {
                    final char c = self.charAt(i);
                    switch (c) {
                    case '"':
                        writer.append('\\').append('"');
                        break;
                    case '\b':
                        writer.append('\\').append('b');
                        break;
                    case '\t':
                        writer.append('\\').append('t');
                        break;
                    case '\n':
                        writer.append('\\').append('n');
                        break;
                    case '\r':
                        writer.append('\\').append('r');
                        break;
                    case '\f':
                        writer.append('\\').append('f');
                        break;
                    case '\'':
                        writer.append('\\').append('\'');
                        break;
                    case '\\':
                        writer.append('\\').append('\\');
                        break;
                    default:
                        writer.append(c);
                    }
                }
                writer.append('"');
            }
        };
    }
    
    static Function writeCharacterFn() {
        return new PrintFn<Character>() {
            @Override
            protected void eval(Character self, Printer writer)
                    throws IOException {
                final char c = self;
                if (!CharClassify.isWhitespace(c)) {
                    writer.append('\\').append(c);
                } else {
                    switch (c) {
                    case '\b':
                        writer.append("\\backspace");
                        break;
                    case '\t':
                        writer.append("\\tab");
                        break;
                    case '\n':
                        writer.append("\\newline");
                        break;
                    case '\r':
                        writer.append("\\return");
                        break;
                    case '\f':
                        writer.append("\\formfeed");
                        break;
                    case ' ':
                        writer.append("\\space");
                        break;
                    default:
                        throw new EdnException("Whitespace character 0x" + Integer.toHexString(c) + " is unsupported.");
                    }
                }
                writer.softspace();
            }
        };
    }
    
    static Function writeLongValueFn() {
        return new PrintFn<Number>() {
            @Override
            protected void eval(Number self, Printer writer)
                    throws IOException {
                writer.softspace()
                    .append(String.valueOf(self.longValue()))
                    .softspace();
            }
        };
    }
    
    static Function writeBigIntegerFn() {
        return new PrintFn<BigInteger>() {
            @Override
            protected void eval(BigInteger self, Printer writer)
                    throws IOException {
                writer.softspace()
                    .append(self.toString()).append('N')
                    .softspace();
            }
        };
    }
    
    static Function writeDoubleValueFn() {
        return new PrintFn<Number>() {
            @Override
            protected void eval(Number self, Printer writer)
                    throws IOException {
                writer.softspace()
                    .append(String.valueOf(self.doubleValue()))
                    .softspace();
            }
        };
    }
    
    static Function writeBigDecimalFn() {
        return new PrintFn<BigDecimal>() {
            @Override
            protected void eval(BigDecimal self, Printer writer)
                    throws IOException {
                writer.softspace()
                    .append(self.toString()).append('M')
                    .softspace();
            }
        };
    }
    
    static Function writeUuidFn() {
        return new PrintFn<UUID>() {
            @Override
            protected void eval(UUID self, Printer writer) throws IOException {
                writer.printValue(Parser.Config.EDN_UUID)
                    .printValue(self.toString());
            }
        };
    }
    
    static Function writeDateFn() {
        return new PrintFn<Date>() {
            @Override
            protected void eval(Date self, Printer writer) throws IOException {
                writer.printValue(Parser.Config.EDN_INSTANT)
                    .printValue(InstantUtils.dateToString(self));
            }
        };
    }
    
    static Function writeTimestampFn() {
        return new PrintFn<Timestamp>() {
            @Override
            protected void eval(Timestamp self, Printer writer) throws IOException {
                writer.printValue(Parser.Config.EDN_INSTANT)
                    .printValue(InstantUtils.timestampToString(self));
            }
        };
    }
    
    static Function writeCalendarFn() {
        return new PrintFn<GregorianCalendar>() {
            @Override
            protected void eval(GregorianCalendar self, Printer writer) throws IOException {
                writer.printValue(Parser.Config.EDN_INSTANT)
                    .printValue(InstantUtils.calendarToString(self));
            }
        };
    }
    
    static Function writeTagFn() {
        return new PrintFn<Tag>() {
            @Override
            protected void eval(Tag self, Printer writer) throws IOException {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

}
