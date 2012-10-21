package us.bpsm.edn.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

import us.bpsm.edn.EdnIOException;
import us.bpsm.edn.Keyword;
import us.bpsm.edn.Symbol;
import us.bpsm.edn.Tag;

/**
 * A Scanner knows how to read syntactically correct edn tokens from any
 * Parseable. Use {@link Scanners#newScanner()} to get an instance.
 */
public interface Scanner {

    /**
     * The next token read from the given {@link Parseable} may be any of the
     * members of {@link Token} or instances of the Java classes used to
     * represent atomic values recognized by Scanner.
     * 
     * <p>
     * The value {@link Token#END_OF_INPUT} marks the end of input, indicating
     * that the underlying {@link Parseable} has been fully consumed.
     * 
     * <p>
     * In addition to the members of {@link Token}, {@code nextToken(…)} may
     * return any of the following:
     * 
     * <ul>
     * <li>A {@link String} with the contents of a string literal.</li>
     * <li>A {@link Character} for a character literal.</li>
     * <li>A {@link Boolean} for a 'true' or 'false'.
     * <li>A {@link Long} for an integer small enough to fit in its range and
     * not marked by a trailing 'N'.</li>
     * <li>A {@link BigInteger} for an integer too large to fit in a Long or
     * marked by a trailing 'N'.</li>
     * <li>A {@link Double} for a binary floating point literal</li>
     * <li>A {@link BigDecimal} for an arbitrary precision decimal floating
     * point literal, which is indicated by a trailing 'M' in edn.</li>
     * <li>A {@link Symbol} for an edn symbol. ('nil', 'true' and 'false' are
     * not symbols.)</li>
     * <li>A {@link Keyword} for an edn keyword, which looks like
     * {@code :somename}.</li>
     * <li>A {@link Tag} indicating that the next value parsed from the TokenSeq
     * should be transformed by a function associated with this Tag.</li>
     * </ul>
     * 
     * @throws EdnIOException
     *             if the underlying Parseable throws an IOException.
     * @throws EdnException
     *             if the contents of the underlying Parseable violates the
     *             syntax of edn.
     * 
     * @return the next token read from the {@link Parseable} (never
     *         {@code null}).
     */
    public Object nextToken(Parseable pbr);

}