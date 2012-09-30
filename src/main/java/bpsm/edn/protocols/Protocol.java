package bpsm.edn.protocols;

public interface Protocol {
    String name();
    
    @SuppressWarnings("rawtypes")
    Function lookup(Class selfClass);

    public interface Builder {
        @SuppressWarnings("rawtypes")
        Builder put(Class selfClass, Function fn);

        Protocol build();
    }
}
