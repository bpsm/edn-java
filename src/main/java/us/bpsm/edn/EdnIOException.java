package us.bpsm.edn;

import java.io.IOException;

/**
 * Indicates that an I/O error occurred. The cause will be an
 * {@link IOException}.
 */
public class EdnIOException extends EdnException {
    private static final long serialVersionUID = 1L;

    public EdnIOException(String msg, IOException cause) {
        super(msg, cause);
    }

    public EdnIOException(IOException cause) {
        super(cause);
    }

    @Override
    public IOException getCause() {
        return (IOException) super.getCause();
    }

}
