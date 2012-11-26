// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn;

/**
 * EdnException is thrown when something goes wrong during the
 * operation of edn-java.  During parsing, this generally, the
 * indicates some kind of syntax error in the input source
 * (see {@link EdnSyntaxException}), or an I/O error (see
 * {@link EdnIOException}).
 */
public class EdnException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EdnException() {
        super();
    }

    public EdnException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public EdnException(String msg) {
        super(msg);
    }

    public EdnException(Throwable cause) {
        super(cause);
    }
}
