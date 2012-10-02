package bpsm.edn.protocols;

public interface Function {
    public Object eval(Object self, Object argument);
}
