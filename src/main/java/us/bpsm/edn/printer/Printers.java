// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.printer;

import java.io.IOException;
import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

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


/**
 * Factory for creating {@link Printer}s and related Objects.
 */
public class Printers {

    private Printers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a new Printer with the default printing
     * protocol. Everything the printer prints will be appended to
     * {@code out}. {@link Printer#close()} will close {@code
     * out}, provided {@code out} implements {@link Closeable}.
     *
     * @param out to which values will be printed. Never null.
     *
     * @return a Printer with default configuration, never null.
     */
    public static Printer newPrinter(final Appendable out) {
        return newPrinter(defaultPrinterProtocol(), out);
    }

    /**
     * Print {@code ednValue} to a new String using the default
     * printing protocol.
     *
     * @param ednValue the value to be returned as a String in edn syntax.
     *
     * @return A string in edn syntax. Not null, not empty.
     */
    public static String printString(Object ednValue) {
        return printString(defaultPrinterProtocol(), ednValue);
    }

    /**
     * Print {@code ednValue} to a new String using the printing
     * protocol given as {@code fns}.
     *
     * @param fns a Protocol which knows how to print all the classes
     *        of objects that we'll be asking our Printer to print.
     *        Never null. Never null.
     *
     * @param ednValue the value to be returned as a String in edn syntax.
     *
     * @return A string in edn syntax. Not null, not empty.
     */
    public static String printString(final Protocol<Printer.Fn<?>> fns,
            Object ednValue) {
        StringBuilder sb = new StringBuilder();
        newPrinter(fns, sb).printValue(ednValue);
        return sb.toString();
    }

    /**
     * Return a new Printer with the printing protocol given as {@code
     * fns}. Everything the printer prints will be appended to {@code
     * writer}. {@link Printer#close()} will close {@code out}, if
     * {@code out} implements {@link Closeable}.
     *
     * @param fns a Protocol which knows how to print all the classes
     *        of objects that we'll be asking our Printer to print.
     *        Never null. Never null.
     * @param out to which values will be printed. Never null.
     *
     * @return a Printer, never null.
     */
    public static Printer newPrinter(final Protocol<Printer.Fn<?>> fns,
            final Appendable out) {
        return new Printer() {
            int softspace = 0;

            @Override
			public void close() {
                if (out instanceof Closeable) {
                    try {
                        ((Closeable)out).close();
                    } catch (IOException e) {
                        throw new EdnIOException(e);
                    }
                }
            }

            @Override
			public Printer append(CharSequence csq) {
                try {
                    if (softspace > 1 && csq.length() > 0 &&
                            !CharClassify.isWhitespace(csq.charAt(0))) {
                        out.append(' ');
                    }
                    softspace = 0;
                    out.append(csq);
                    return this;
                } catch (IOException e) {
                    throw new EdnIOException(e);
                }
            }

            @Override
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

            @Override
			public Printer printValue(Object ednValue) {
                @SuppressWarnings("unchecked")
                Printer.Fn<Object> printFn = (Printer.Fn<Object>)
                fns.lookup(getClassOrNull(ednValue));
                if (printFn == null) {
                    throw new EdnException(String.format(
                            "Don't know how to write '%s' of type '%s'",
                            ednValue, getClassOrNull(ednValue)));
                }
                printFn.eval(ednValue, this);
                return this;
            }

            @Override
			public Printer softspace() {
                softspace += 1;
                return this;
            }

        };
    }



    static Class<?> getClassOrNull(Object o) {
        return o == null ? null : o.getClass();
    }

    /**
     * Returns a {@link us.bpsm.edn.protocols.Protocol.Builder}
     * configured to produce a Protocol which knows how to print
     * these types of values:
     *
     * <ul>
     * <li>{@link BigDecimal}</li>
     * <li>{@link BigInteger}</li>
     * <li>{@link Boolean}</li>
     * <li>{@link Byte} (as an integer)</li>
     * <li>{@link CharSequence} (as a string literal)</li>
     * <li>{@link Character} (as a character literal)</li>
     * <li>{@link Date} (as {@code #inst})</li>
     * <li>{@link Double}</li>
     * <li>{@link Float}</li>
     * <li>{@link GregorianCalendar} (as {@code #inst})</li>
     * <li>{@link Integer}</li>
     * <li>{@link Keyword}</li>
     * <li>{@link List}</li>
     * <li>{@link Long}</li>
     * <li>{@link Map}</li>
     * <li>{@link Set}</li>
     * <li>{@link Short} (as an integer)</li>
     * <li>{@link Symbol}</li>
     * <li>{@link Tag}</li>
     * <li>{@link TaggedValue}</li>
     * <li>{@link java.sql.Timestamp} (as {@code #inst})</li>
     * <li>{@link UUID} (as {@code #uuid})</li>
     * </ul>
     * @return a Protocol.Builder initialized with the default implementations
     *         for printing.
     */
    public static Protocol.Builder<Printer.Fn<?>> defaultProtocolBuilder() {
        return Protocols.<Printer.Fn<?>>builder("print")
                .put(null, writeNullFn())
                .put(BigDecimal.class, writeBigDecimalFn())
                .put(BigInteger.class, writeBigIntegerFn())
                .put(Boolean.class, writeBooleanFn())
                .put(Byte.class, writeLongValueFn())
                .put(CharSequence.class, writeCharSequenceFn())
                .put(Character.class, writeCharacterFn())
                .put(Date.class, writeDateFn())
                .put(Double.class, writeDoubleValueFn())
                .put(Float.class, writeDoubleValueFn())
                .put(GregorianCalendar.class, writeCalendarFn())
                .put(Integer.class, writeLongValueFn())
                .put(Keyword.class, writeKeywordFn())
                .put(List.class, writeListFn())
                .put(Long.class, writeLongValueFn())
                .put(Map.class, writeMapFn())
                .put(Set.class, writeSetFn())
                .put(Short.class, writeLongValueFn())
                .put(Symbol.class, writeSymbolFn())
                .put(Tag.class, writeTagFn())
                .put(TaggedValue.class, writeTaggedValueFn())
                .put(Timestamp.class, writeTimestampFn())
                .put(UUID.class, writeUuidFn());
    }

    /**
     * Return the default printer {@link Protocol}. This is equivalent
     * to {@code defaultProtocolBuilder().build()}.
     *
     * @return the default printing {@link Protocol}, never null.
     */
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
                    case ',':
                        // The comma is classified as whitespace by isWhitespace
                        // because it is such for purposes of parsing.
                        writer.append("\\,");
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

    static final class PrettyPrintContext {
        int depth = 0;
        String basicIndent = "  ";
        List<String> indents = new ArrayList<String>(Arrays.asList(""));
    }

    private static final ThreadLocal<PrettyPrintContext> PRETTY_PRINT_CONTEXT = new ThreadLocal<PrettyPrintContext>();


    static void printIndent(Printer p) {
        PrettyPrintContext cx = PRETTY_PRINT_CONTEXT.get();
        p.append(cx.indents.get(cx.depth));
    }

    static void withPretty(Runnable r) {
        final boolean shouldInit = (PRETTY_PRINT_CONTEXT.get() == null);
        if (shouldInit) {
            PRETTY_PRINT_CONTEXT.set(new PrettyPrintContext());
            try {
                r.run();
            } finally {
                PRETTY_PRINT_CONTEXT.remove();
            }
        } else {
            r.run();
        }
    }

    static void runIndented(Runnable r) {
        PrettyPrintContext cx = PRETTY_PRINT_CONTEXT.get();
        assert cx.depth < cx.indents.size();
        if (cx.indents.size() - cx.depth == 1) {
            cx.indents.add(cx.indents.get(cx.depth) + cx.basicIndent);
        }
        cx.depth += 1;
        assert cx.depth < cx.indents.size();

        try {
            r.run();
        } finally {
            cx.depth -= 1;
        }
    }


    static Printer.Fn<List<?>> prettyWriteListFn() {
        return new Printer.Fn<List<?>>() {
            @Override
            public void eval(final List<?> self, final Printer writer) {
                withPretty(new Runnable() {
                    @Override
                    public void run() {
                        boolean vec = self instanceof RandomAccess;
                        writer.append(vec ? '[' : '(');
                        writer.append("\n");
                        runIndented(new Runnable() {
                            @Override
                            public void run() {
                                for (Object o: self) {
                                    printIndent(writer);
                                    writer.printValue(o);
                                    writer.append("\n");
                                }
                            }
                        });
                        printIndent(writer);
                        writer.append(vec ? ']' : ')');
                    }
                });
            }
        };
    }

    static Printer.Fn<Set<?>> prettyWriteSetFn() {
        return new Printer.Fn<Set<?>>() {
            @Override
            public void eval(final Set<?> self, final Printer writer) {
                withPretty(new Runnable() {
                    @Override
                    public void run() {
                        writer.append("#{");
                        writer.append("\n");
                        runIndented(new Runnable() {
                            @Override
                            public void run() {
                                for (Object o : self) {
                                    printIndent(writer);
                                    writer.printValue(o);
                                    writer.append("\n");
                                }
                            }
                        });
                        printIndent(writer);
                        writer.append("}");
                    }
                });
            }
        };
    }

    static Printer.Fn<Map<?, ?>> prettyWriteMapFn() {
        return new Printer.Fn<Map<?,?>>() {
            @Override
            public void eval(final Map<?,?> self, final Printer writer) {
                withPretty(new Runnable() {
                    @Override
                    public void run() {
                        writer.append("{");
                        writer.append("\n");
                        runIndented(new Runnable() {
                            @Override
                            public void run() {
                                for (Map.Entry<?,?> o: self.entrySet()) {
                                    printIndent(writer);
                                    writer.printValue(o.getKey());
                                    writer.softspace();
                                    writer.softspace();
                                    writer.printValue(o.getValue());
                                    writer.append("\n");
                                }
                            }
                        });
                        printIndent(writer);
                        writer.append("}");
                    }
                });
            }
        };
    }

    public static Protocol.Builder<Printer.Fn<?>> prettyProtocolBuilder() {
        return defaultProtocolBuilder()
                .put(Map.class, prettyWriteMapFn())
                .put(Set.class, prettyWriteSetFn())
                .put(List.class, prettyWriteListFn());
    }

    public static Protocol<Printer.Fn<?>> prettyPrinterProtocol() {
        return prettyProtocolBuilder().build();
    }


}
