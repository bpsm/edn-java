package bpsm.edn.parser;

import java.util.HashMap;
import java.util.Map;

class Interner<T> {

    Map<T,T> m = new HashMap<T,T>();
    
    public T intern(T t) {
        T interned = m.get(t);
        if (interned != null) {
            return interned;
        }
        m.put(t, t);
        return t;
    }
    
}
