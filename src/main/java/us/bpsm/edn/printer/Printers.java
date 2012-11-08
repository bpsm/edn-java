// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.printer;

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

import us.bpsm.edn.EdnException;
import us.bpsm.edn.EdnIOException;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.Tag;
import us.bpsm.edn.TaggedValue;
import us.bpsm.edn.parser.InstantUtils;
import us.bpsm.edn.parser.Parser;
import us.bpsm.edn.protocols.Protocol;
import us.bpsm.edn.protocols.Protocols;
import us.bpsm.edn.util.CharClassify;


public class Printers {

    public static Printer newPrinter(final Writer writer) {
        return newPrinter(defaultPrinterProtocol(), writer);
    }

    public static String printString(final Protocol<Printer.Fn<?>> fns, Object ednValue) {
        StringBuilder sb = new StringBuilder();
        newPrinter(fns, sb).printValue(ednValue);
        return sb.toString();
    }

    public static String printString(Object ednValue) {
        return printString(defaultPrinterProtocol(), ednValue);
    }

    public static Printer newPrinter(final Protocol<Printer.Fn<?>> fns, final Appendable out) {
        return new Printer() {
            int softspace = 0;

            public void close() {
                if (out instanceof Writer) {
                    try {
                        ((Writer)out).close();
                    } catch (IOException e) {
                        throw new EdnIOException(e);
                    }
                }
            }

            public Printer append(CharSequence csq) {
                try {
                    if (softspace > 1 && csq.length() > 0 && !CharClassify.isWhitespace(csq.charAt(0))) {
                        out.append(' ');
                    }
                    softspace = 0;
                    out.append(csq);
                    return this;
                } catch (IOException e) {
                    throw new EdnIOException(e);
                }
            }

            public Printer append(char c) {
                try {
                    if (softspace > 1 && !CharClassify.isWhitespace(c)) {
                        out.append(' ');
                    }
                    softspace = 0;
                    out.append(c);
                    return this;
                } catch (IOException e) {
                    throw new EdnIOException(e);
                }
            }

            public Printer printValue(Object ednValue) {
                @SuppressWarnings("unchecked")
                Printer.Fn<Object> printFn = (Printer.Fn<Object>) fns.lookup(getClassOrNull(ednValue));
                if (printFn == null) {
                    throw new EdnException(String.format(
                            "Don't know how to write '%s' of type '%s'",
                            ednValue, getClassOrNull(ednValue)));
                }
                printFn.eval(ednValue, this);
                return this;
            }

            public Printer softspace() {
                softspace += 1;
                return this;
            }

        };
    }

    static Class<?> getClassOrNull(Object o) {
        return o == null ? null : o.getClass();
    }

    public static Protocol.Builder<Printer.Fn<?>> defaultProtocolBuilder() {
        Protocol.Builder<Printer.Fn<?>> protocolBuilder =
                Protocols.<Printer.Fn<?>>builder("print")
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
                .put(UUID.class, writeUuidFn()).put(Date.class, writeDateFn())
                .put(Timestamp.class, writeTimestampFn())
                .put(GregorianCalendar.class, writeCalendarFn())
                .put(TaggedValue.class, writeTaggedValueFn())
                .put(Tag.class, writeTagFn());
        return protocolBuilder;
    }


    public static Protocol<Printer.Fn<?>> defaultPrinterProtocol() {
        return defaultProtocolBuilder().build();
    }



    static Printer.Fn<Void> writeNullFn() {
        return new Printer.Fn<Void>() {
            @Override
            public void eval(Void self, Printer writer) {
                writer.softspace().append("nil").softspace();
            }
        };
    }

    static Printer.Fn<List<?>> writeListFn() {
        return new Printer.Fn<List<?>>() {
            @Override
            public void eval(List<?> self, Printer writer) {
                boolean vec = self instanceof RandomAccess;
                writer.append(vec ? '[' : '(');
                for (Object o: self) {
                    writer.printValue(o);
                }
                writer.append(vec ? ']' : ')');
            }
        };
    }

    static Printer.Fn<Set<?>> writeSetFn() {
        return new Printer.Fn<Set<?>>() {
            @Override
            public void eval(Set<?> self, Printer writer) {
                writer.append("#{");
                for (Object o: self) {
                    writer.printValue(o);
                }
                writer.append('}');
            }
        };
    }

    static Printer.Fn<Map<?, ?>> writeMapFn() {
        return new Printer.Fn<Map<?,?>>() {
            @Override
            public void eval(Map<?,?> self, Printer writer) {
                writer.append('{');
                for (Map.Entry<?,?> p: self.entrySet()) {
                    writer.printValue(p.getKey())
                    .printValue(p.getValue());
                }
                writer.append('}');
            }
        };
    }

    static Printer.Fn<Keyword> writeKeywordFn() {
        return new Printer.Fn<Keyword>() {
            @Override
            public void eval(Keyword self, Printer writer) {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

    static Printer.Fn<Symbol> writeSymbolFn() {
        return new Printer.Fn<Symbol>() {
            @Override
            public void eval(Symbol self, Printer writer) {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

    static Printer.Fn<TaggedValue> writeTaggedValueFn() {
        return new Printer.Fn<TaggedValue>() {
            @Override
            public void eval(TaggedValue self, Printer writer) {
                writer.printValue(self.getTag()).printValue(self.getValue());
            }
        };
    }

    static Printer.Fn<Boolean> writeBooleanFn() {
        return new Printer.Fn<Boolean>() {
            @Override
            public void eval(Boolean self, Printer writer) {
                writer.softspace()
                .append(self ? "true" : "false")
                .softspace();
            }
        };
    }

    static Printer.Fn<CharSequence> writeCharSequenceFn() {
        return new Printer.Fn<CharSequence>() {
            @Override
            public void eval(CharSequence self, Printer writer) {
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

    static Printer.Fn<Character> writeCharacterFn() {
        return new Printer.Fn<Character>() {
            @Override
            public void eval(Character self, Printer writer) {
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

    static Printer.Fn<Number> writeLongValueFn() {
        return new Printer.Fn<Number>() {
            @Override
            public void eval(Number self, Printer writer) {
                writer.softspace()
                .append(String.valueOf(self.longValue()))
                .softspace();
            }
        };
    }

    static Printer.Fn<BigInteger> writeBigIntegerFn() {
        return new Printer.Fn<BigInteger>() {
            @Override
            public void eval(BigInteger self, Printer writer) {
                writer.softspace()
                .append(self.toString()).append('N')
                .softspace();
            }
        };
    }

    static Printer.Fn<Number> writeDoubleValueFn() {
        return new Printer.Fn<Number>() {
            @Override
            public void eval(Number self, Printer writer) {
                writer.softspace()
                .append(String.valueOf(self.doubleValue()))
                .softspace();
            }
        };
    }

    static Printer.Fn<BigDecimal> writeBigDecimalFn() {
        return new Printer.Fn<BigDecimal>() {
            @Override
            public void eval(BigDecimal self, Printer writer) {
                writer.softspace()
                .append(self.toString()).append('M')
                .softspace();
            }
        };
    }

    static Printer.Fn<UUID> writeUuidFn() {
        return new Printer.Fn<UUID>() {
            @Override
            public void eval(UUID self, Printer writer) {
                writer.printValue(Parser.Config.EDN_UUID)
                .printValue(self.toString());
            }
        };
    }

    static Printer.Fn<Date> writeDateFn() {
        return new Printer.Fn<Date>() {
            @Override
            public void eval(Date self, Printer writer) {
                writer.printValue(Parser.Config.EDN_INSTANT)
                .printValue(InstantUtils.dateToString(self));
            }
        };
    }

    static Printer.Fn<Timestamp> writeTimestampFn() {
        return new Printer.Fn<Timestamp>() {
            @Override
            public void eval(Timestamp self, Printer writer) {
                writer.printValue(Parser.Config.EDN_INSTANT)
                .printValue(InstantUtils.timestampToString(self));
            }
        };
    }

    static Printer.Fn<GregorianCalendar> writeCalendarFn() {
        return new Printer.Fn<GregorianCalendar>() {
            @Override
            public void eval(GregorianCalendar self, Printer writer) {
                writer.printValue(Parser.Config.EDN_INSTANT)
                .printValue(InstantUtils.calendarToString(self));
            }
        };
    }

    static Printer.Fn<Tag> writeTagFn() {
        return new Printer.Fn<Tag>() {
            @Override
            public void eval(Tag self, Printer writer) {
                writer.softspace().append(self.toString()).softspace();
            }
        };
    }

}
