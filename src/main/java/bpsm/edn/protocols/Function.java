// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.protocols;

public interface Function {
    public Object eval(Object self, Object argument);
}
