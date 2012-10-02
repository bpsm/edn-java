// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.printer;

class PrintException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PrintException() {
        super();
    }

    public PrintException(String arg0) {
        super(arg0);
    }

    public PrintException(Throwable arg0) {
        super(arg0);
    }

    public PrintException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

}
